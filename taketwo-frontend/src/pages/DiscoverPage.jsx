import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import MovieCard from '../components/MovieCard.jsx';
import * as movieService from '../services/movieService';
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

export default function DiscoverPage() {
  const [searchParams] = useSearchParams();
  const query = searchParams.get('q');

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
          const [trendingRes, popularRes] = await Promise.all([
            movieService.getTrending('week'),
            movieService.getPopular(1),
          ]);
          if (!cancelled) {
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