// pages/TakeTwoPage.jsx
import { useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api';
import styles from './TakeTwoPage.module.css';

const GENRES = ['Action', 'Comedy', 'Drama', 'Horror', 'Romance', 'Science Fiction', 'Thriller', 'Mystery', 'Fantasy', 'Animation', 'Crime', 'Documentary'];
const RUNTIME_OPTIONS = [
  { label: 'Any length', value: null },
  { label: 'Under 90 min', value: 90 },
  { label: 'Under 2 hours', value: 120 },
  { label: 'Under 2.5 hours', value: 150 },
];

export default function TakeTwoPage() {
  const [genre, setGenre] = useState(null);
  const [runtime, setRuntime] = useState(null);
  const [familiarity, setFamiliarity] = useState('EITHER');
  const [popularity, setPopularity] = useState('EITHER');

  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  async function handleSubmit() {
    if (!genre) return;
    setLoading(true);
    setError('');
    setResult(null);
    try {
      const res = await api.post('/taketwo/recommend', {
        genre,
        maxRuntimeMinutes: runtime,
        familiarity,
        popularity,
      });
      setResult(res.data);
    } catch (err) {
      setError(err.response?.data?.message || 'Could not find a match right now.');
    } finally {
      setLoading(false);
    }
  }

  function handleReset() {
    setResult(null);
    setGenre(null);
  }

  return (
    <div className={styles.page}>
      <h1 className={styles.title}>Take Two</h1>
      <p className={styles.subtitle}>Answer a few questions, get one film - no scrolling required.</p>

      {!result && (
        <div className={styles.wizard}>
          <div className={styles.step}>
            <h3>What are you in the mood for?</h3>
            <div className={styles.chipGrid}>
              {GENRES.map((g) => (
                <button
                  key={g}
                  className={`${styles.chip} ${genre === g ? styles.chipActive : ''}`}
                  onClick={() => setGenre(g)}
                >
                  {g}
                </button>
              ))}
            </div>
          </div>

          <div className={styles.step}>
            <h3>How much time do you have?</h3>
            <div className={styles.chipGrid}>
              {RUNTIME_OPTIONS.map((opt) => (
                <button
                  key={opt.label}
                  className={`${styles.chip} ${runtime === opt.value ? styles.chipActive : ''}`}
                  onClick={() => setRuntime(opt.value)}
                >
                  {opt.label}
                </button>
              ))}
            </div>
          </div>

          <div className={styles.step}>
            <h3>Familiar or something new?</h3>
            <div className={styles.chipGrid}>
              {[['FAMILIAR', 'Stick with what I know'], ['NEW', 'Surprise me'], ['EITHER', 'No preference']].map(([val, label]) => (
                <button
                  key={val}
                  className={`${styles.chip} ${familiarity === val ? styles.chipActive : ''}`}
                  onClick={() => setFamiliarity(val)}
                >
                  {label}
                </button>
              ))}
            </div>
          </div>

          <div className={styles.step}>
            <h3>Popular pick or hidden gem?</h3>
            <div className={styles.chipGrid}>
              {[['POPULAR', 'Popular'], ['HIDDEN_GEM', 'Hidden gem'], ['EITHER', 'No preference']].map(([val, label]) => (
                <button
                  key={val}
                  className={`${styles.chip} ${popularity === val ? styles.chipActive : ''}`}
                  onClick={() => setPopularity(val)}
                >
                  {label}
                </button>
              ))}
            </div>
          </div>

          <button className={styles.submitBtn} disabled={!genre || loading} onClick={handleSubmit}>
            {loading ? 'Finding your film...' : 'Take Two'}
          </button>

          {error && <p className={styles.error}>{error}</p>}
        </div>
      )}

      {result && (
        <div className={styles.result}>
          <div className={styles.matchBadge}>{result.matchPercent}% match</div>
          {result.movie.posterUrl && (
            <img src={result.movie.posterUrl} alt={result.movie.title} className={styles.poster} />
          )}
          <h2>{result.movie.title}</h2>
          <p className={styles.explanation}>{result.explanation}</p>
          <div className={styles.tags}>
            {result.reasonTags.map((tag) => <span key={tag} className={styles.tag}>{tag}</span>)}
          </div>
          <div className={styles.resultActions}>
            <Link to={`/movie/${result.movie.tmdbId}`} className={styles.primaryBtn}>View film</Link>
            <button className={styles.secondaryBtn} onClick={handleReset}>Try again</button>
          </div>
        </div>
      )}
    </div>
  );
}