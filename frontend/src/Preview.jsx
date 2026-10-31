import { useEffect, useMemo, useState } from 'react';
const RUNTIME = String.raw`<!doctype html><html><head><meta charset="utf-8"><meta http-equiv="Content-Security-Policy" content="default-src 'none'; script-src 'unsafe-inline' 'unsafe-eval' __ORIGIN__; style-src 'unsafe-inline'; img-src data: blob:; font-src data:; connect-src 'none'; form-action 'none'; base-uri 'none'"><style>body{font:14px system-ui;color:#20242c;background:#fff;margin:0;padding:20px}*{box-sizing:border-box}#runtime-error{white-space:pre-wrap;color:#b42332;background:#fff3f3;border:1px solid #f1c4c8;padding:12px;border-radius:7px;font:12px/1.5 ui-monospace,monospace}</style><script src="__ORIGIN__/runtime/react.js"></script><script src="__ORIGIN__/runtime/react-dom.js"></script><script src="__ORIGIN__/runtime/babel.js"></script></head><body><div id="root"></div><script>
const token = "__TOKEN__";
const rootNode = document.getElementById("root");
let reactRoot;
function report(type, message) { parent.postMessage({ channel: "reactcode-preview", token, type, message }, "*"); }
function showError(error) {
  const message = error && error.message ? error.message : String(error);
  rootNode.innerHTML = "";
  const box = document.createElement("pre"); box.id = "runtime-error"; box.textContent = message;
  rootNode.appendChild(box); report("error", message);
}
window.addEventListener("error", event => showError(event.error || event.message || "Runtime error"));
window.addEventListener("unhandledrejection", event => showError(event.reason || "Unhandled promise rejection"));
window.addEventListener("message", event => {
  if (event.source !== parent || event.data?.channel !== "reactcode-source" || event.data?.token !== token) return;
  try {
    const transformed = Babel.transform(event.data.code, { presets: [["react", { runtime: "classic" }], ["env", { modules: "commonjs" }]] }).code;
    const module = { exports: {} };
    const require = name => name === "react" ? React : ((name === "react-dom" || name === "react-dom/client") ? ReactDOM : (() => { throw new Error("Unsupported import: " + name); })());
    new Function("module", "exports", "require", "React", "ReactDOM", transformed)(module, module.exports, require, React, ReactDOM);
    const Component = module.exports.default || module.exports;
    if (typeof Component !== "function") throw new Error("Export a default React component.");
    rootNode.innerHTML = "";
    reactRoot = reactRoot || ReactDOM.createRoot(rootNode);
    reactRoot.render(React.createElement(Component));
    report("ready", "Preview updated");
  } catch (error) { showError(error); }
});
report("booted", "Preview runtime ready");
</script></body></html>`;

export default function Preview({ code }) {
  const token = useMemo(() => crypto.randomUUID(), []);
  const [loaded, setLoaded] = useState(false);
  const [status, setStatus] = useState({ kind: 'loading', message: 'Starting isolated preview…' });
  const srcDoc = useMemo(() => RUNTIME
    .replaceAll('__ORIGIN__', window.location.origin)
    .replace('__TOKEN__', token), [token]);

  useEffect(() => {
    const onMessage = (event) => {
      if (event.source !== document.getElementById('reactcode-preview')?.contentWindow
          || event.data?.channel !== 'reactcode-preview' || event.data?.token !== token) return;
      if (event.data.type === 'booted') setStatus({ kind: 'loading', message: 'Compiling preview…' });
      else setStatus({ kind: event.data.type === 'error' ? 'error' : 'ready', message: event.data.message });
    };
    window.addEventListener('message', onMessage);
    return () => window.removeEventListener('message', onMessage);
  }, [token]);

  useEffect(() => {
    if (!loaded) return;
    setStatus({ kind: 'loading', message: 'Compiling preview…' });
    const timer = setTimeout(() => {
      document.getElementById('reactcode-preview')?.contentWindow?.postMessage({ channel: 'reactcode-source', token, code }, '*');
    }, 180);
    return () => clearTimeout(timer);
  }, [code, loaded, token]);

  return <div className="preview-wrap">
    <div className="preview-toolbar"><span className="preview-label"><i /> Live preview</span><span className={`preview-status ${status.kind}`}>{status.kind === 'ready' ? 'Running' : status.kind === 'loading' ? 'Loading' : 'Error'}</span></div>
    <iframe id="reactcode-preview" title="Isolated React preview" sandbox="allow-scripts" srcDoc={srcDoc} onLoad={() => setLoaded(true)} />
    {status.kind !== 'ready' && <div className={`preview-message ${status.kind}`}>{status.kind === 'loading' && <span className="spinner" />}{status.message}</div>}
  </div>;
}
