import express from 'express';
import { readFileSync } from 'node:fs';
import { transformSync } from '@babel/core';
import presetEnv from '@babel/preset-env';
import presetReact from '@babel/preset-react';
import { chromium } from 'playwright';

const app = express();
app.disable('x-powered-by');
app.use(express.json({ limit: '120kb', strict: true }));
let activeEvaluation = false;

const base64 = (value) => Buffer.from(value, 'utf8').toString('base64');
const reactBundle = base64(readFileSync(new URL('./node_modules/react/umd/react.development.js', import.meta.url), 'utf8'));
const reactDomBundle = base64(readFileSync(new URL('./node_modules/react-dom/umd/react-dom.development.js', import.meta.url), 'utf8'));

function makeFrameHtml(compiledCode) {
  const script = `
    (() => {
      const root = document.getElementById('root');
      const showError = (error) => {
        document.body.dataset.runnerError = 'true';
        root.innerHTML = '';
        const pre = document.createElement('pre');
        pre.dataset.runnerError = 'true';
        pre.textContent = error?.message || String(error);
        root.appendChild(pre);
      };
      window.addEventListener('error', (event) => showError(event.error || event.message));
      window.addEventListener('unhandledrejection', (event) => showError(event.reason));
      try {
        (0, eval)(atob('${reactBundle}'));
        (0, eval)(atob('${reactDomBundle}'));
        const module = { exports: {} };
        const require = (name) => {
          if (name === 'react') return window.React;
          if (name === 'react-dom') return window.ReactDOM;
          throw new Error('Unsupported import: ' + name);
        };
        new Function('module', 'exports', 'require', 'React', 'ReactDOM', atob('${base64(compiledCode)}'))(
          module, module.exports, require, window.React, window.ReactDOM
        );
        const Component = module.exports.default || module.exports;
        if (typeof Component !== 'function') throw new Error('Export a default React component.');
        window.ReactDOM.createRoot(root).render(window.React.createElement(Component));
        document.body.dataset.runnerReady = 'true';
      } catch (error) { showError(error); }
    })();`;
  return `<!doctype html><html><head><meta charset="utf-8"><meta http-equiv="Content-Security-Policy" content="default-src 'none'; script-src 'unsafe-inline' 'unsafe-eval'; style-src 'unsafe-inline'; img-src data: blob:; font-src data:; connect-src 'none'; form-action 'none'; base-uri 'none'"><style>body{font:14px system-ui,sans-serif;margin:0;padding:12px;color:#111;background:white}*{box-sizing:border-box}</style></head><body><div id="root"></div><script>${script}</script></body></html>`;
}

async function renderCase(browser, code, testCase) {
  const page = await browser.newPage({ javaScriptEnabled: true });
  page.setDefaultTimeout(2500);
  await page.route('**/*', (route) => route.request().url().startsWith('about:') ? route.continue() : route.abort());
  const frameHtml = makeFrameHtml(code);
  await page.setContent(`<iframe id="solution" sandbox="allow-scripts" title="submitted component" srcdoc="${frameHtml.replace(/&/g, '&amp;').replace(/"/g, '&quot;')}"></iframe>`, { waitUntil: 'domcontentloaded', timeout: 2500 });
  const frame = page.frameLocator('#solution');
  const errorPanel = frame.locator('[data-runner-error]');
  try {
    await Promise.race([
      frame.locator('body[data-runner-ready="true"], [data-runner-error]').first().waitFor({ state: 'attached', timeout: 2500 }),
      new Promise((_, reject) => setTimeout(() => reject(new Error('Component render timed out.')), 2800)),
    ]);
    if (await errorPanel.count()) throw new Error(`Component error: ${(await errorPanel.innerText()).slice(0, 240)}`);
    for (const step of testCase.steps || []) await runStep(frame, step);
    return { id: testCase.id, name: testCase.name, passed: true, message: null };
  } catch (error) {
    let runtimeMessage = '';
    try { if (await errorPanel.count()) runtimeMessage = (await errorPanel.innerText()).slice(0, 180); } catch {}
    return { id: testCase.id, name: testCase.name, passed: false, message: runtimeMessage || normalizeError(error) };
  } finally {
    await page.close();
  }
}

function target(frame, step, keepAll = false) {
  if (step.selector) {
    const locator = frame.locator(step.selector);
    return step.index == null ? (keepAll ? locator : locator.first()) : locator.nth(step.index);
  }
  if (step.testId) return keepAll ? frame.getByTestId(step.testId) : frame.getByTestId(step.testId).first();
  if (step.text) return frame.getByText(step.text, { exact: true }).first();
  throw new Error('Test step is missing a target.');
}

async function runStep(frame, step) {
  const locator = step.type === 'wait' || step.type === 'drag' ? null : target(frame, step, step.type === 'assertCount');
  switch (step.type) {
    case 'click': await locator.click(); break;
    case 'type': await locator.fill(String(step.value ?? '')); break;
    case 'assertText': {
      const actual = (await locator.textContent())?.trim() ?? '';
      const expected = String(step.equals);
      const matches = step.contains == null ? actual === expected : actual.includes(String(step.contains));
      if (!matches) throw new Error(`Expected text ${step.contains == null ? `“${expected}”` : `to contain “${step.contains}”`}, received “${actual}”.`);
      break;
    }
    case 'assertExists':
      if (!(await locator.count())) throw new Error('Expected the element to exist.');
      break;
    case 'assertNotExists':
      if (await locator.count()) throw new Error('Expected the element to be absent.');
      break;
    case 'assertAttribute': {
      const actual = await locator.getAttribute(step.name);
      if (actual !== String(step.equals)) throw new Error(`Expected ${step.name} to be “${step.equals}”, received “${actual}”.`);
      break;
    }
    case 'assertValue': {
      const actual = await locator.inputValue();
      if (actual !== String(step.equals)) throw new Error(`Expected value “${step.equals}”, received “${actual}”.`);
      break;
    }
    case 'assertCount': {
      const actual = await locator.count();
      if (actual !== Number(step.equals)) throw new Error(`Expected ${step.equals} matching elements, received ${actual}.`);
      break;
    }
    case 'key': await locator.press(String(step.key)); break;
    case 'wait': await new Promise((resolve) => setTimeout(resolve, Math.min(Math.max(Number(step.ms) || 0, 0), 1000))); break;
    case 'drag': {
      const from = frame.locator(step.from).nth(step.fromIndex || 0);
      const to = frame.locator(step.to).nth(step.toIndex || 0);
      await from.dragTo(to);
      break;
    }
    default: throw new Error(`Unsupported test step: ${step.type}`);
  }
}

function normalizeError(error) {
  const message = String(error?.message || error || 'Test failed');
  return message.replace(/\s+/g, ' ').slice(0, 220);
}

app.get('/health', (_request, response) => response.json({ status: 'ok' }));

app.post('/evaluate', async (request, response) => {
  if (request.get('X-Evaluator-Token') !== (process.env.EVALUATOR_TOKEN || 'local-dev-only-change-me')) {
    return response.status(401).json({ message: 'Unauthorized evaluator request.' });
  }
  if (activeEvaluation) return response.status(429).json({ message: 'The evaluator is busy. Retry shortly.' });
  const { problemSlug, sourceCode, tests } = request.body || {};
  const cases = tests?.cases;
  if (typeof problemSlug !== 'string' || !/^[a-z0-9-]{1,64}$/.test(problemSlug)
      || typeof sourceCode !== 'string' || !sourceCode.trim() || sourceCode.length > 100_000
      || !Array.isArray(cases) || cases.length < 1 || cases.length > 30
      || cases.some((item) => !item || typeof item.id !== 'string' || typeof item.name !== 'string' || !Array.isArray(item.steps) || item.steps.length > 100)) {
    return response.status(400).json({ message: 'Invalid evaluation request.' });
  }
  if (cases.reduce((total, item) => total + item.steps.length, 0) > 100) {
    return response.status(400).json({ message: 'Evaluation contains too many test steps.' });
  }
  let compiled;
  try {
    compiled = transformSync(sourceCode, {
      filename: 'solution.jsx',
      presets: [[presetEnv, { targets: { chrome: '120' }, modules: 'commonjs' }], [presetReact, { runtime: 'classic' }]],
      sourceType: 'module',
      configFile: false,
      babelrc: false,
    }).code;
  } catch (error) {
    const results = cases.map((item) => ({ id: item.id, name: item.name, passed: false, message: `Compilation error: ${normalizeError(error)}` }));
    return response.json({ passedCount: 0, failedCount: results.length, totalCount: results.length, results });
  }
  let browser;
  let responseBody;
  let responseStatus = 200;
  let evaluationTimeout;
  activeEvaluation = true;
  try {
    browser = await chromium.launch({ headless: true, chromiumSandbox: false, args: ['--disable-dev-shm-usage'] });
    const results = [];
    const runCases = async () => {
      for (const testCase of cases) results.push(await renderCase(browser, compiled, testCase));
      return results;
    };
    const timedOut = new Promise((_, reject) => {
      evaluationTimeout = setTimeout(() => reject(new Error('Evaluation exceeded its 30 second time limit.')), 30_000);
    });
    await Promise.race([runCases(), timedOut]);
    const passedCount = results.filter((item) => item.passed).length;
    responseBody = { passedCount, failedCount: results.length - passedCount, totalCount: results.length, results };
  } catch (error) {
    if (String(error?.message).includes('30 second time limit')) {
      const results = cases.map((item) => ({ id: item.id, name: item.name, passed: false, message: 'Evaluation timed out after 30 seconds.' }));
      responseBody = { passedCount: 0, failedCount: results.length, totalCount: results.length, results };
    } else {
      console.error('Evaluation runner failure:', normalizeError(error));
      responseStatus = 503;
      responseBody = { message: 'The isolated browser runner could not complete the evaluation.' };
    }
  } finally {
    clearTimeout(evaluationTimeout);
    if (browser) await browser.close().catch(() => {});
    activeEvaluation = false;
  }
  return response.status(responseStatus).json(responseBody);
});

const port = Number(process.env.PORT || 8090);
app.listen(port, '0.0.0.0', () => console.log(`BugForge evaluator listening on ${port}`));
