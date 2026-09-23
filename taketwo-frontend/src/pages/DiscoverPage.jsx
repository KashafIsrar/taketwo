import { useEffect, useState } from 'react';
import { useSearchParams, Link } from 'react-router-dom';
import MovieCard from '../components/MovieCard.jsx';
import * as movieService from '../services/movieService';
import { getActivityFeed } from '../services/api';
import styles from './DiscoverPage.module.css';

function MovieGrid({ movies }) {
  return (
    <div className={styles.grid}>
      {movies.map((movie) => (
        <MovieCard key={movie.tmdbId} movie={movie} />
      ))}
    </div>
  );
}

function ActivityFeed({ feed }) {
  if (!feed || feed.length === 0) {
    return (
      <div className={styles.emptyFeed}>
        <p>No friend activity yet. Follow other members to see their logs here!</p>
      </div>
    );
  }

  return (
    <div className={styles.feedList}>
      {feed.map((log) => (
        <div key={log.id} className={styles.feedCard}>
          <div className={styles.feedHeader}>
            <span className={styles.username}>
              <Link to={`/user/${log.user?.id}`}>{log.user?.username || 'Someone'}</Link>
            </span>
            <span className={styles.actionText}> watched </span>
            <span className={styles.movieTitle}>{log.movie?.title}</span>
            <span className={styles.rating}> ★ {log.rating}</span>
          </div>
          {log.review && <p className={styles.review}>"{log.review}"</p>}
          <span className={styles.date}>{new Date(log.watchedDate).toLocaleDateString()}</span>
        </div>
      ))}
    </div>
  );
}

export default function DiscoverPage() {
  const [searchParams] = useSearchParams();
  const query = searchParams.get('q');

  const [feed, setFeed] = useState([]);
  const [trending, setTrending] = useState([]);
  const [popular, setPopular] = useState([]);
  const [searchResults, setSearchResults] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError('');

    async function load() {
      try {
        if (query) {
          const results = await movieService.searchMovies(query);
          if (!cancelled) setSearchResults(results);
        } else {
          const [feedRes, trendingRes, popularRes] = await Promise.all([
            getActivityFeed().catch(() => []),
            movieService.getTrending('week'),
            movieService.getPopular(1),
          ]);
          if (!cancelled) {
            setFeed(feedRes);
            setTrending(trendingRes);
            setPopular(popularRes);
          }
        }
      } catch (err) {
        if (!cancelled) setError(err.response?.data?.message || 'Could not load movies right now. Try again shortly.');
      } finally {
        if (!cancelled) setLoading(false);
      }
    }

    load();
    return () => { cancelled = true; };
  }, [query]);

  if (loading) {
    return <div className={styles.page}><p className={styles.state}>Loading…</p></div>;
  }

  if (error) {
    return <div className={styles.page}><p className={styles.state}>{error}</p></div>;
  }

  if (query) {
    return (
      <div className={styles.page}>
        <div className={styles.section}>
          <h2 className={styles.sectionTitle}>Results for "{query}"</h2>
          {searchResults.length ? (
            <MovieGrid movies={searchResults} />
          ) : (
            <p className={styles.state}>No movies found.</p>
          )}
        </div>
      </div>
    );
  }

  return (
    <div className={styles.page}>
      <p className={styles.tagline}>Find your next obsession.</p>

      {/* Activity Feed Section */}
      <div className={styles.section}>
        <h2 className={styles.sectionTitle}>Friend Activity</h2>
        <ActivityFeed feed={feed} />
      </div>

      <div className={styles.section}>
        <h2 className={styles.sectionTitle}>Trending this week</h2>
        <MovieGrid movies={trending} />
      </div>

      <div className={styles.section}>
        <h2 className={styles.sectionTitle}>Popular</h2>
        <MovieGrid movies={popular} />
      </div>
    </div>
  );
}