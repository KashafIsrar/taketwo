import { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext.jsx';
import StarRating from '../components/StarRating.jsx';
import MovieCard from '../components/MovieCard.jsx';
import * as movieService from '../services/movieService';
import styles from './DiaryPage.module.css';

function SpoilerReview({ text, containsSpoilers }) {
  const [revealed, setRevealed] = useState(!containsSpoilers);

  if (!containsSpoilers) {
    return <p className={styles.reviewText}>{text}</p>;
  }

  return (
    <div className={styles.spoilerWrap}>
      <span className={styles.spoilerLabel} onClick={() => setRevealed((r) => !r)}>
        {revealed ? 'Hide spoilers' : 'Contains spoilers — click to reveal'}
      </span>
      <p className={`${styles.reviewText} ${revealed ? '' : styles.spoilerBlur}`}>{text}</p>
    </div>
  );
}

function DiaryList({ logs }) {
  if (!logs.length) return <p className={styles.state}>No logged films yet — go find something to watch.</p>;

  return (
    <div>
      {logs.map((log) => (
        <div key={log.logId} className={styles.logRow}>
          {log.movie.posterUrl ? (
            <img className={styles.logPoster} src={log.movie.posterUrl} alt={log.movie.title} />
          ) : (
            <div className={styles.logPoster} />
          )}
          <div className={styles.logMain}>
            <div className={styles.logHeader}>
              <span className={styles.logTitle}>
                {log.movie.title}
                {log.rewatch && <span className={styles.rewatchBadge}>Rewatch</span>}
              </span>
              <span className={styles.logDate}>{log.watchedDate}</span>
            </div>
            <StarRating value={log.rating} readOnly size={16} />
            {log.reviewText && (
              <SpoilerReview text={log.reviewText} containsSpoilers={log.containsSpoilers} />
            )}
          </div>
        </div>
      ))}
    </div>
  );
}

function WatchlistGrid({ items }) {
  if (!items.length) return <p className={styles.state}>Your watchlist is empty.</p>;

  return (
    <div className={styles.watchlistGrid}>
      {items.map((item) => (
        <MovieCard key={item.movie.tmdbId} movie={item.movie} />
      ))}
    </div>
  );
}

export default function DiaryPage() {
  const { user } = useAuth();
  const [tab, setTab] = useState('diary');
  const [logs, setLogs] = useState([]);
  const [watchlist, setWatchlist] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);

    Promise.all([
      movieService.getUserDiary(user.userId),
      movieService.getUserWatchlist(user.userId),
    ])
      .then(([logsRes, watchlistRes]) => {
        if (!cancelled) {
          setLogs(logsRes);
          setWatchlist(watchlistRes);
        }
      })
      .finally(() => { if (!cancelled) setLoading(false); });

    return () => { cancelled = true; };
  }, [user.userId]);

  return (
    <div className={styles.page}>
      <div className={styles.tabs}>
        <button
          className={`${styles.tab} ${tab === 'diary' ? styles.activeTab : ''}`}
          onClick={() => setTab('diary')}
        >
          Diary ({logs.length})
        </button>
        <button
          className={`${styles.tab} ${tab === 'watchlist' ? styles.activeTab : ''}`}
          onClick={() => setTab('watchlist')}
        >
          Watchlist ({watchlist.length})
        </button>
      </div>

      {loading ? (
        <p className={styles.state}>Loading…</p>
      ) : tab === 'diary' ? (
        <DiaryList logs={logs} />
      ) : (
        <WatchlistGrid items={watchlist} />
      )}
    </div>
  );
}
