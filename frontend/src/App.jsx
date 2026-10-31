import { useCallback, useEffect, useState } from 'react';
import Editor from '@monaco-editor/react';
import { Link, Navigate, Route, Routes, useNavigate, useParams } from 'react-router-dom';
import { api, complete, draftKey, getCompleted } from './api.js';
import Preview from './Preview.jsx';

function Icon({ name, size = 18 }) {
  const paths = {
    code: <><path d="m8 17-5-5 5-5"/><path d="m16 7 5 5-5 5"/><path d="m14 4-4 16"/></>,
    search: <><circle cx="11" cy="11" r="7"/><path d="m20 20-4-4"/></>,
    arrow: <><path d="M5 12h14"/><path d="m12 5 7 7-7 7"/></>,
    back: <><path d="M19 12H5"/><path d="m12 19-7-7 7-7"/></>,
    check: <path d="m5 12 4 4L19 6"/>,
    play: <path d="m8 5 12 7-12 7z"/>,
    reset: <><path d="M3 12a9 9 0 1 0 2.6-6.4L3 8"/><path d="M3 3v5h5"/></>,
    panel: <><rect x="3" y="4" width="18" height="16" rx="2"/><path d="M9 4v16"/></>,
  };
  return <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">{paths[name]}</svg>;
}

function Difficulty({ value }) {
  return <span className={`difficulty ${String(value || 'easy').toLowerCase()}`}>{String(value || 'easy').toLowerCase()}</span>;
}

function Header({ count = 0, total = 10, compact = false }) {
  return <header className="topbar">
    <Link to="/" className="brand"><span className="brand-mark"><Icon name="code" size={19} /></span><span>Bug<span className="brand-accent">Forge</span></span></Link>
    <div className="topbar-right">
      {!compact && <span className="header-label">COMPONENT PRACTICE</span>}
      <div className="progress-chip"><span className="progress-dot" /> <b>{count}/{total}</b><span>completed</span></div>
    </div>
  </header>;
}

function HomePage() {
  const [problems, setProblems] = useState([]);
  const [completed, setCompleted] = useState(getCompleted);
  const [query, setQuery] = useState('');
  const [difficulty, setDifficulty] = useState('all');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    api('/api/problems?catalog=mvp&page=0&size=50')
      .then((page) => setProblems(page.content || []))
      .catch((reason) => setError(reason.message))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    const update = () => setCompleted(getCompleted());
    window.addEventListener('reactcode:completion', update);
    window.addEventListener('storage', update);
    return () => { window.removeEventListener('reactcode:completion', update); window.removeEventListener('storage', update); };
  }, []);

  const visible = problems.filter((problem) => {
    const matchesQuery = `${problem.title} ${problem.shortDescription} ${problem.slug}`.toLowerCase().includes(query.toLowerCase());
    return matchesQuery && (difficulty === 'all' || String(problem.difficulty).toLowerCase() === difficulty);
  });

  return <div className="app-shell">
    <Header count={completed.size} total={problems.length || 10} />
    <main className="home-main">
      <div className="hero-row"><div><div className="eyebrow"><span className="eyebrow-line" /> YOUR REACT WORKOUT</div><h1>Build it. <span>Break it.</span> Ship it.</h1><p className="hero-copy">Small components. Real React patterns. One challenge at a time.</p></div><div className="hero-metric"><span className="metric-number">{String(completed.size).padStart(2, '0')}</span><span className="metric-caption">OF {String(problems.length || 10).padStart(2, '0')} SOLVED</span><div className="metric-track"><i style={{ width: `${Math.min(100, completed.size / Math.max(problems.length, 10) * 100)}%` }} /></div></div></div>
      <section className="challenge-card">
        <div className="list-heading"><div><span className="section-kicker">THE PRACTICE SET</span><h2>React challenges <span className="count-badge">{problems.length || 10}</span></h2></div><div className="list-controls"><label className="search-box"><Icon name="search" size={16}/><input aria-label="Search challenges" placeholder="Find a challenge..." value={query} onChange={(event) => setQuery(event.target.value)} /></label><select aria-label="Filter by difficulty" value={difficulty} onChange={(event) => setDifficulty(event.target.value)}><option value="all">All levels</option><option value="easy">Easy</option><option value="medium">Medium</option><option value="hard">Hard</option></select></div></div>
        <div className="table-head"><span>PROBLEM</span><span>DIFFICULTY</span><span>STATUS</span><span /></div>
        {loading && <div className="empty-state"><span className="spinner"/> Loading challenges…</div>}
        {error && <div className="error-banner">Could not load challenges: {error}<button onClick={() => window.location.reload()}>Retry</button></div>}
        {!loading && !error && visible.length === 0 && <div className="empty-state">No challenges match those filters.</div>}
        {visible.map((problem, index) => <Link className="problem-row" to={`/problem/${problem.slug}`} key={problem.slug}>
          <span className="problem-title-cell"><span className="problem-index">{String(problem.sortOrder || index + 1).padStart(2, '0')}</span><span className={`row-check ${completed.has(problem.slug) ? 'done' : ''}`}>{completed.has(problem.slug) && <Icon name="check" size={14}/>}</span><span className="problem-text"><b>{problem.title}</b><small>{problem.shortDescription}</small></span></span>
          <span><Difficulty value={problem.difficulty}/></span>
          <span className={`status-text ${completed.has(problem.slug) ? 'solved' : ''}`}>{completed.has(problem.slug) ? 'Solved' : 'Not started'}</span>
          <span className="row-arrow"><Icon name="arrow" size={16}/></span>
        </Link>)}
        <footer className="list-footer"><span><i className="online-dot"/> {problems.length || 0} challenges available</span><span>Pick one and start building <span className="footer-spark">✳</span></span></footer>
      </section>
      <div className="home-note"><Icon name="panel" size={16}/> Your code drafts are saved in this browser automatically.</div>
    </main>
  </div>;
}

function Results({ result }) {
  if (!result) return <div className="result-placeholder"><span className="result-icon"><Icon name="play" size={16}/></span><div><b>Ready when you are</b><small>Submit your component to run the test suite.</small></div></div>;
  const passed = result.status === 'PASSED' || result.passed === true;
  const cases = result.results || [];
  return <div className={`result-block ${passed ? 'pass' : 'fail'}`}>
    <div className="result-summary"><span className="result-symbol"><Icon name={passed ? 'check' : 'code'} size={16}/></span><div><b>{passed ? 'All tests passed' : 'Some tests failed'}</b><small>{result.passedCount ?? result.passedTests ?? 0} of {result.totalCount ?? result.totalTests ?? cases.length} tests passed</small></div></div>
    {cases.map((test, index) => <div className="test-result" key={test.id || test.name || index}><span className={test.passed ? 'test-check' : 'test-cross'}>{test.passed ? '✓' : '×'}</span><span>{test.name || test.id || `Test ${index + 1}`}</span>{test.message && <small>{test.message}</small>}</div>)}
    {result.verificationNote && <p className="verification-note">{result.verificationNote}</p>}
  </div>;
}

function ProblemPage() {
  const { slug } = useParams();
  const navigate = useNavigate();
  const [problem, setProblem] = useState(null);
  const [code, setCode] = useState('');
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [saved, setSaved] = useState(false);
  const [completed, setCompleted] = useState(getCompleted);

  useEffect(() => {
    let active = true;
    setLoading(true); setError(''); setResult(null);
    api(`/api/problems/${encodeURIComponent(slug)}`)
      .then((detail) => {
        if (!active) return;
        setProblem(detail);
        setCode(localStorage.getItem(draftKey(slug)) ?? detail.starterCode ?? '');
      })
      .catch((reason) => { if (active) setError(reason.message); })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, [slug]);

  useEffect(() => {
    if (!problem) return undefined;
    let hide;
    const timer = setTimeout(() => {
      localStorage.setItem(draftKey(slug), code);
      setSaved(true);
      hide = setTimeout(() => setSaved(false), 1200);
    }, 350);
    return () => { clearTimeout(timer); clearTimeout(hide); };
  }, [code, problem, slug]);

  const submit = useCallback(async () => {
    if (!problem || submitting) return;
    setSubmitting(true); setError('');
    try {
      const response = await api('/api/submissions', { method: 'POST', body: JSON.stringify({ challengeSlug: slug, code }) });
      setResult(response);
      if (response.status === 'PASSED' || response.passed === true) {
        complete(slug);
          setCompleted(getCompleted());
        window.dispatchEvent(new Event('reactcode:completion'));
      }
    } catch (reason) { setError(reason.message); }
    finally { setSubmitting(false); }
  }, [problem, submitting, slug, code]);

  useEffect(() => {
    const onKeyDown = (event) => {
      if ((event.metaKey || event.ctrlKey) && event.key === 'Enter') {
        event.preventDefault();
        submit();
      }
    };
    window.addEventListener('keydown', onKeyDown);
    return () => window.removeEventListener('keydown', onKeyDown);
  }, [submit]);

  if (loading) return <div className="app-shell"><Header count={completed.size} total={10} compact/><div className="screen-state"><span className="spinner"/> Loading challenge…</div></div>;
  if (error && !problem) return <div className="app-shell"><Header count={completed.size} total={10} compact/><div className="screen-state error-screen"><b>We couldn’t open this challenge</b><p>{error}</p><Link to="/">Back to challenges</Link></div></div>;
  if (!problem) return <Navigate to="/" replace/>;

  return <div className="workspace-shell">
    <header className="workspace-topbar"><Link to="/" className="brand"><span className="brand-mark"><Icon name="code" size={19}/></span><span>react<span className="brand-accent">code</span></span></Link><div className="workspace-center"><button className="back-button" onClick={() => navigate('/')} aria-label="Back to challenges"><Icon name="back" size={17}/></button><span className="crumb-divider">/</span><span className="workspace-challenge">{String(problem.sortOrder).padStart(2, '0')} <b>{problem.title}</b></span><Difficulty value={problem.difficulty}/></div><div className="workspace-actions"><span className={`save-indicator ${saved ? 'visible' : ''}`}><span/> Draft saved</span><button className="reset-button" onClick={() => { setCode(problem.starterCode || ''); setResult(null); }}><Icon name="reset" size={15}/> Reset</button><button className="submit-button" disabled={submitting} onClick={submit}>{submitting ? <span className="spinner"/> : <Icon name="play" size={14}/>} {submitting ? 'Running…' : 'Submit'}</button></div></header>
    <main className="workspace-grid">
      <section className="pane statement-pane"><div className="pane-heading"><span><i className="pane-dot purple"/> PROBLEM</span><span className="pane-menu">•••</span></div><div className="statement-scroll"><div className="problem-heading"><div className="problem-number">CHALLENGE {String(problem.sortOrder).padStart(2, '0')}</div><h1>{problem.title}</h1><Difficulty value={problem.difficulty}/></div><div className="statement-content" dangerouslySetInnerHTML={{ __html: markdown(problem.statement) }}/><div className="statement-tip"><span>✳</span><div><b>Think in components</b><p>Keep your UI predictable and your state close to where it is used.</p></div></div><div className="statement-meta"><span><Icon name="code" size={14}/> React · JavaScript</span><span>~15 min</span></div></div></section>
      <section className="pane editor-pane"><div className="pane-heading"><span><i className="pane-dot green"/> EDITOR</span><span className="language-chip">JSX <span>⌄</span></span></div><div className="editor-area"><Editor height="100%" language="javascript" theme="vs-dark" value={code} onChange={(value) => setCode(value ?? '')} options={{ minimap: { enabled: false }, fontSize: 13, fontFamily: 'JetBrains Mono, Consolas, monospace', lineNumbers: 'on', scrollBeyondLastLine: false, tabSize: 2, wordWrap: 'on', padding: { top: 16 }, automaticLayout: true, suggestOnTriggerCharacters: true, quickSuggestions: true, bracketPairColorization: { enabled: true } }} /></div><div className="editor-footer"><span><i className="footer-dot"/> JavaScript JSX</span><span>⌘ + Enter to submit</span></div></section>
      <section className="pane preview-pane"><div className="pane-heading"><span><i className="pane-dot cyan"/> PREVIEW</span><span className="preview-device">DESKTOP <span>⌄</span></span></div><div className="preview-canvas"><Preview code={code}/></div><div className="results-section"><div className="results-heading"><span>TEST RESULTS</span><span className={result ? 'results-count active' : 'results-count'}>{result ? `${result.passedCount ?? 0}/${result.totalCount ?? (result.results || []).length}` : 'NOT RUN'}</span></div><Results result={result}/>{error && <div className="inline-error">{error}</div>}</div></section>
    </main>
  </div>;
}

function markdown(text = '') {
  return text.split('\n').map((line, i) => {
    const trimmed = line.trim();
    if (!trimmed) return '<div class="md-space"></div>';
    if (trimmed.startsWith('### ')) return `<h3>${inline(trimmed.slice(4))}</h3>`;
    if (trimmed.startsWith('## ')) return `<h2>${inline(trimmed.slice(3))}</h2>`;
    if (trimmed.startsWith('# ')) return `<h2>${inline(trimmed.slice(2))}</h2>`;
    if (trimmed.startsWith('- ')) return `<div class="md-list-item"><span>•</span><p>${inline(trimmed.slice(2))}</p></div>`;
    if (/^\d+\. /.test(trimmed)) return `<div class="md-list-item"><span>${trimmed.match(/^\d+/)[0]}.</span><p>${inline(trimmed.replace(/^\d+\. /, ''))}</p></div>`;
    return `<p>${inline(trimmed)}</p>`;
  }).join('');
}

function inline(text) {
  return text.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/`([^`]+)`/g, '<code>$1</code>').replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>');
}

function App() {
  return <Routes><Route path="/" element={<HomePage/>} /><Route path="/problem/:slug" element={<ProblemPage/>} /><Route path="*" element={<Navigate to="/" replace/>}/></Routes>;
}

export default App;
