import "./App.css";
import { useEffect, useState } from "react";

const API = import.meta.env.VITE_API_BASE_URL;
const DEFAULTS = { symbol: "RELIANCE", from: "2026-08-01", to: "2026-08-31" };
const NAV_ITEMS = [
  { id: "Overview", label: "Overview" },
  { id: "Market Data", label: "Markets" },
  { id: "Portfolio", label: "Portfolio" },
  { id: "Backtesting", label: "Backtesting" },
  { id: "Risk Analysis", label: "Risk" },
  { id: "Experiments", label: "Experiments" },
];

const VIEW_META = {
  Overview: { title: "Financial Market Intelligence", subtitle: "Analyze markets, test ideas, and measure risk with reproducible quantitative research." },
  "Market Data": { title: "Market data", subtitle: "Inspect clean OHLCV observations across your research universe." },
  Portfolio: { title: "Virtual portfolio", subtitle: "Track simulated exposure and understand the path from price to portfolio value." },
  Backtesting: { title: "Strategy backtesting", subtitle: "Evaluate execution-aware strategies against historical market data." },
  "Risk Analysis": { title: "Risk analysis", subtitle: "Turn historical behavior into practical, observable risk measures." },
  Experiments: { title: "Experiment library", subtitle: "Review the configurations and results behind your research runs." },
};

const percent = (value) => `${(Number(value || 0) * 100).toFixed(2)}%`;
const money = (value) => `₹${Number(value || 0).toLocaleString("en-IN", { maximumFractionDigits: 0 })}`;

function App() {
  const [view, setView] = useState("Overview");
  const [securities, setSecurities] = useState([]);
  const [marketData, setMarketData] = useState([]);
  const [analytics, setAnalytics] = useState(null);
  const [experiments, setExperiments] = useState([]);
  const [filters, setFilters] = useState(DEFAULTS);
  const [backtest, setBacktest] = useState(null);
  const [error, setError] = useState("");

  const loadMarket = async (nextFilters = filters) => {
    const query = new URLSearchParams({ from: nextFilters.from, to: nextFilters.to });
    const [marketResponse, analyticsResponse] = await Promise.all([
      fetch(`${API}/api/market-data/${nextFilters.symbol}?${query}`),
      fetch(`${API}/api/analytics/${nextFilters.symbol}?${query}`),
    ]);
    if (!marketResponse.ok || !analyticsResponse.ok) throw new Error("Market data is unavailable for this range.");
    setMarketData(await marketResponse.json());
    setAnalytics(await analyticsResponse.json());
  };

  const loadExperiments = async () => {
    const response = await fetch(`${API}/api/experiments`);
    if (response.ok) setExperiments(await response.json());
  };

  useEffect(() => {
    const query = new URLSearchParams({ from: DEFAULTS.from, to: DEFAULTS.to });
    Promise.all([
      fetch(`${API}/api/securities`),
      fetch(`${API}/api/market-data/${DEFAULTS.symbol}?${query}`),
      fetch(`${API}/api/analytics/${DEFAULTS.symbol}?${query}`),
      fetch(`${API}/api/experiments`),
    ])
      .then(async ([securityResponse, marketResponse, analyticsResponse, experimentsResponse]) => {
        if (!securityResponse.ok || !marketResponse.ok || !analyticsResponse.ok) throw new Error("Backend data is unavailable.");
        setSecurities(await securityResponse.json());
        setMarketData(await marketResponse.json());
        setAnalytics(await analyticsResponse.json());
        if (experimentsResponse.ok) setExperiments(await experimentsResponse.json());
      })
      .catch((loadError) => setError(loadError.message));
  }, []);

  const runBacktest = async (event) => {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const request = {
      symbol: form.get("symbol"), from: form.get("from"), to: form.get("to"), strategy: form.get("strategy"),
      shortSma: Number(form.get("shortSma")), longSma: Number(form.get("longSma")),
      initialCapital: Number(form.get("initialCapital")), transactionCost: Number(form.get("transactionCost")), slippage: Number(form.get("slippage")),
    };
    try {
      const response = await fetch(`${API}/api/backtests`, { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify(request) });
      if (!response.ok) throw new Error((await response.json()).error || "Backtest failed.");
      setBacktest(await response.json());
    } catch (runError) { setError(runError.message); }
  };

  const saveExperiment = async () => {
    if (!backtest) return;
    const configuration = {
      symbol: backtest.symbol, from: backtest.from, to: backtest.to, strategy: backtest.strategy,
      shortSma: backtest.shortSma, longSma: backtest.longSma, initialCapital: backtest.initialCapital,
      transactionCost: backtest.transactionCost, slippage: backtest.slippage,
    };
    const response = await fetch(`${API}/api/experiments`, { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify(configuration) });
    if (response.ok) { await loadExperiments(); setView("Experiments"); }
  };

  const chartPoints = marketData.map((item, index) => {
    const prices = marketData.map((data) => data.close);
    const minPrice = Math.min(...prices); const maxPrice = Math.max(...prices);
    const x = marketData.length === 1 ? 400 : (index / (marketData.length - 1)) * 800;
    const y = 260 - ((item.close - minPrice) / (maxPrice - minPrice || 1)) * 220;
    return `${x},${y}`;
  }).join(" ");

  const renderChart = () => <div className="chart-shell"><div className="chart-axis"><span>High</span><span>Low</span></div><div className="chart">{marketData.length ? <svg viewBox="0 0 800 300" preserveAspectRatio="none"><polyline className="line" points={chartPoints} /></svg> : <p className="muted">No observations in this range.</p>}</div></div>;
  const marketPanel = <div className="panel chart-panel"><div className="panel-header"><div><span className="section-kicker">PRICE SERIES</span><h2>{filters.symbol} closing price</h2><p>{filters.from} to {filters.to} · {marketData.length} observations</p></div><div className="chart-value"><span>Last close</span><strong>{marketData.length ? money(marketData[marketData.length - 1].close) : "--"}</strong></div></div>{renderChart()}<div className="chart-footer"><span>{marketData[0]?.tradingDate || filters.from}</span><span>Daily close</span><span>{marketData[marketData.length - 1]?.tradingDate || filters.to}</span></div></div>;
  const watchlist = <div className="panel watchlist-panel"><div className="panel-header"><div><span className="section-kicker">RESEARCH UNIVERSE</span><h2>Watchlist</h2><p>Choose a security to refresh the workspace</p></div><span className="count-pill">{securities.length} assets</span></div><div className="watchlist">{securities.map((security) => <button className={`watch ${filters.symbol === security.symbol ? "selected" : ""}`} key={security.id} onClick={() => { const next = { ...filters, symbol: security.symbol }; setFilters(next); loadMarket(next).catch((loadError) => setError(loadError.message)); }}><span className="watch-symbol">{security.symbol}</span><span className="watch-company">{security.companyName}</span><span className="watch-exchange">{security.exchange}</span><span className="watch-arrow">→</span></button>)}</div></div>;

  return <div className="app">
    <header className="topbar"><div className="brand"><span className="brand-mark">F</span><span>FinSight</span><small>QUANT LAB</small></div><nav className="topnav">{NAV_ITEMS.map((item) => <button className={view === item.id ? "active" : ""} key={item.id} onClick={() => setView(item.id)}>{item.label}</button>)}</nav><div className="topbar-actions"><span className="system-status"><span className="status-dot" /> System Online</span><button className="icon-button" aria-label="Open settings">⌘</button></div></header>
    <main className="main"><header className="page-header"><div><p className="eyebrow">FINANCIAL ENGINEERING PLATFORM</p><h1>{VIEW_META[view].title}</h1><p className="subtitle">{VIEW_META[view].subtitle}</p></div><button className="experiment-btn" onClick={() => setView("Backtesting")}><span>+</span> New Experiment</button></header>
      {error && <div className="alert">{error}<button onClick={() => setError("")}>Dismiss</button></div>}
      {view === "Overview" && <><section className="stats"><Metric label="Latest Close" value={marketData.length ? money(marketData[marketData.length - 1].close) : "--"} detail={filters.symbol} accent="violet" /><Metric label="Total Return" value={percent(analytics?.totalReturn)} detail="Selected range" accent="green" /><Metric label="Volatility" value={percent(analytics?.volatility)} detail="Annualized" accent="blue" /><Metric label="Max Drawdown" value={percent(analytics?.maxDrawdown)} detail="Historical" accent="orange" /></section><section className="content-grid">{marketPanel}{watchlist}</section><section className="bottom-grid"><ExperimentList experiments={experiments} /><div className="panel insight"><span className="eyebrow">RESEARCH STATUS</span><h2>Simulation environment ready.</h2><p>Use the backtesting workspace to compare assumptions, costs and strategy results.</p><button onClick={() => setView("Backtesting")}>Open Backtesting <span>→</span></button></div></section></>}
      {view === "Market Data" && <><section className="toolbar panel"><label>Security<select value={filters.symbol} onChange={(event) => setFilters({ ...filters, symbol: event.target.value })}>{securities.map((security) => <option key={security.symbol}>{security.symbol}</option>)}</select></label><label>From<input type="date" value={filters.from} onChange={(event) => setFilters({ ...filters, from: event.target.value })} /></label><label>To<input type="date" value={filters.to} onChange={(event) => setFilters({ ...filters, to: event.target.value })} /></label><button onClick={() => loadMarket().catch((loadError) => setError(loadError.message))}>Load Data</button></section><section className="content-grid">{marketPanel}{watchlist}</section><DataTable data={marketData} /></>}
      {view === "Portfolio" && <div className="panel empty-state"><span className="eyebrow">VIRTUAL PORTFOLIO</span><h2>Portfolio state is derived from simulations.</h2><p>Run a backtest to inspect cash, holdings, equity curve and execution costs without connecting to a broker.</p><button onClick={() => setView("Backtesting")}>Configure a backtest</button></div>}
      {view === "Backtesting" && <BacktestView securities={securities} filters={filters} onRun={runBacktest} result={backtest} onSave={saveExperiment} />}
      {view === "Risk Analysis" && <RiskView analytics={analytics} />}
      {view === "Experiments" && <ExperimentList experiments={experiments} full />}
    </main>
  </div>;
}

function Metric({ label, value, detail, accent }) { return <div className={`card ${accent ? `accent-${accent}` : ""}`}><span>{label}</span><strong>{value}</strong><small>{detail}</small></div>; }
function ExperimentList({ experiments, full = false }) { return <div className={`panel ${full ? "wide-panel" : ""}`}><h2>Recent Experiments</h2><p className="muted">Saved configurations and their measured results.</p>{experiments.length ? experiments.map((experiment) => <div className="experiment" key={experiment.id}><div><strong>{experiment.strategy}</strong><span>{experiment.symbol} · {experiment.strategyParameters}</span></div><b>{percent(experiment.totalReturn)}</b></div>) : <p className="muted">No saved experiments yet.</p>}</div>; }
function DataTable({ data }) { return <div className="panel table-wrap"><h2>OHLCV observations</h2><table><thead><tr><th>Date</th><th>Open</th><th>High</th><th>Low</th><th>Close</th><th>Volume</th></tr></thead><tbody>{data.map((item) => <tr key={item.tradingDate}><td>{item.tradingDate}</td><td>{item.open}</td><td>{item.high}</td><td>{item.low}</td><td>{item.close}</td><td>{item.volume.toLocaleString()}</td></tr>)}</tbody></table></div>; }
function BacktestView({ securities, filters, onRun, result, onSave }) { return <><form className="panel form-grid" onSubmit={onRun}><label>Security<select name="symbol" defaultValue={filters.symbol}>{securities.map((security) => <option key={security.symbol}>{security.symbol}</option>)}</select></label><label>From<input name="from" type="date" defaultValue={filters.from} required /></label><label>To<input name="to" type="date" defaultValue={filters.to} required /></label><label>Strategy<select name="strategy" defaultValue="BUY_AND_HOLD"><option value="BUY_AND_HOLD">Buy & Hold</option><option value="SMA_CROSSOVER">SMA Crossover</option></select></label><label>Short SMA<input name="shortSma" type="number" defaultValue="20" min="1" /></label><label>Long SMA<input name="longSma" type="number" defaultValue="50" min="2" /></label><label>Initial capital<input name="initialCapital" type="number" defaultValue="100000" min="1" /></label><label>Fee %<input name="transactionCost" type="number" defaultValue="0.1" min="0" step="0.01" /></label><label>Slippage %<input name="slippage" type="number" defaultValue="0.05" min="0" step="0.01" /></label><button type="submit">Run Backtest</button></form>{result && <div className="panel result-panel"><div className="panel-header"><div><span className="eyebrow">RESULT</span><h2>{result.strategy} · {result.symbol}</h2></div><button onClick={onSave}>Save Experiment</button></div><section className="stats compact"><Metric label="Final value" value={money(result.finalPortfolioValue)} detail={`P&L ${money(result.pnl)}`} /><Metric label="Return" value={percent(result.totalReturn)} detail={`${result.tradeCount} executions`} /><Metric label="Volatility" value={percent(result.volatility)} detail="Annualized" /><Metric label="Drawdown" value={percent(result.maxDrawdown)} detail="Peak to trough" /></section><h3>Equity curve</h3><div className="equity-list">{result.equityCurve.map((point) => <span key={point.date} style={{ height: `${Math.max(8, (point.value / result.initialCapital) * 100)}%` }} title={`${point.date}: ${money(point.value)}`} />)}</div></div>}</>; }
function RiskView({ analytics }) { return <div className="panel risk-grid"><span className="eyebrow">RISK ANALYSIS</span><h2>Observed risk for {analytics ? "the selected range" : "the current selection"}</h2><Metric label="Annualized volatility" value={percent(analytics?.volatility)} detail="Standard deviation of daily returns" /><Metric label="Maximum drawdown" value={percent(analytics?.maxDrawdown)} detail="Largest peak-to-trough decline" /><Metric label="Historical VaR" value={percent(analytics?.valueAtRisk)} detail="95% one-day loss threshold" /><Metric label="Sharpe ratio" value={Number(analytics?.sharpeRatio || 0).toFixed(2)} detail="Zero risk-free rate assumption" /></div>; }

export default App;