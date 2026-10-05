import { useEffect, useState, useRef } from 'react';
import { useSearchParams } from 'react-router-dom';
import MovieCard from '../components/MovieCard.jsx';
import TakeTwoCallout from '../components/TakeTwoCallout.jsx';
import * as movieService from '../services/movieService';
import api from '../services/api';
import styles from './DiscoverPage.module.css';

const GENRES = [
  { id: 'all', name: 'All' },
  { id: '28', name: 'Action' },
  { id: '878', name: 'Sci-Fi' },
  { id: '35', name: 'Comedy' },
  { id: '18', name: 'Drama' },
  { id: '27', name: 'Horror' },
  { id: '10749', name: 'Romance' }
];

function MovieGrid({ movies }) {
  return (
    <div className={styles.grid}>
      {movies.map((movie) => (
        <MovieCard key={movie.tmdbId || movie.id} movie={movie} />
      ))}
    </div>
  );
}

export default function DiscoverPage() {
  const [searchParams] = useSearchParams();
  const query = searchParams.get('q');

  const [selectedGenre, setSelectedGenre] = useState('all');
  const [nowPlaying, setNowPlaying] = useState([]);
  const [trending, setTrending] = useState([]);
  const [popular, setPopular] = useState([]);
  const [topRated, setTopRated] = useState([]);
  const [genreResults, setGenreResults] = useState([]);
  const [searchResults, setSearchResults] = useState([]);
  
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // Carousels refs
  const nowPlayingRef = useRef(null);
  const trendingRef = useRef(null);
  const popularRef = useRef(null);
  const topRatedRef = useRef(null);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError('');

    async function loadData() {
      try {
        if (query) {
          const results = await movieService.searchMovies(query);
          if (!cancelled) setSearchResults(results);
        } else {
          // Fetch multiple distinct categories concurrently
          const [trendingRes, popularRes, nowPlayingRes, topRatedRes] = await Promise.all([
            movieService.getTrending('week').catch(() => []),
            movieService.getPopular(1).catch(() => []),
            api.get('/movies/discover?primary_release_year=2026').catch(() => ({ data: [] })),
            api.get('/movies/discover?sort_by=vote_average.desc&vote_count.gte=500').catch(() => ({ data: [] }))
          ]);

          if (!cancelled) {
            setTrending(trendingRes);
            setPopular(popularRes);
            setNowPlaying(nowPlayingRes.data.results || nowPlayingRes.data || []);
            setTopRated(topRatedRes.data.results || topRatedRes.data || []);
          }
        }
      } catch (err) {
        if (!cancelled) {
          setError('Could not load discovery feeds right now.');
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    }

    loadData();
    return () => { cancelled = true; };
  }, [query]);

  // Handle genre filter changes safely - multi-page fetch for a rich genre view
  useEffect(() => {
    if (selectedGenre === 'all' || query) return;

    let cancelled = false;

    async function fetchGenreMovies() {
      try {
        let combinedMovies = [];
        // Fetch pages 1 through 3 for the selected genre to ensure a robust, full list
        for (let page = 1; page <= 3; page++) {
          const res = await api.get(`/movies/discover?with_genres=${selectedGenre}&page=${page}`);
          const data = res.data.results || res.data || [];
          if (data.length === 0) break; // Stop if page is empty
          combinedMovies = [...combinedMovies, ...data];
        }

        if (!cancelled) {
          // Deduplicate by tmdbId/id
          const uniqueMovies = Array.from(
            new Map(combinedMovies.map(m => [m.tmdbId || m.id, m])).values()
          );
          setGenreResults(uniqueMovies);
        }
      } catch {
        if (!cancelled) setGenreResults([]);
      }
    }

    fetchGenreMovies();

    return () => { cancelled = true; };
  }, [selectedGenre, query]);

  const scroll = (ref, direction) => {
    if (ref.current) {
      const scrollAmount = direction === 'left' ? -600 : 600;
      ref.current.scrollBy({ left: scrollAmount, behavior: 'smooth' });
    }
  };

  const renderCarousel = (title, movies, ref) => {
    if (!movies || movies.length === 0) return null;

    return (
      <div className={styles.categorySection} key={title}>
        <h2 className={styles.sectionTitle}>{title}</h2>
        <div className={styles.carouselWrapper}>
          <button className={`${styles.scrollBtn} ${styles.left}`} onClick={() => scroll(ref, 'left')}>
            &#10094;
          </button>
          
          <div className={styles.carousel} ref={ref}>
            {movies.map((movie) => (
              <div key={movie.tmdbId || movie.id} className={styles.carouselCardWrapper}>
                <MovieCard movie={movie} />
              </div>
            ))}
          </div>

          <button className={`${styles.scrollBtn} ${styles.right}`} onClick={() => scroll(ref, 'right')}>
            &#10095;
          </button>
        </div>
      </div>
    );
  };

  if (loading) return <div className={styles.page}><p className={styles.state}>Loading Discover Feeds…</p></div>;
  if (error) return <div className={styles.page}><p className={styles.state}>{error}</p></div>;

  if (query) {
    return (
      <div className={styles.page}>
        <div className={styles.section}>
          <h2 className={styles.sectionTitle}>Results for "{query}"</h2>
          {searchResults.length ? <MovieGrid movies={searchResults} /> : <p className={styles.state}>No movies found.</p>}
        </div>
      </div>
    );
  }

  return (
    <div className={styles.page}>
      {/* Hero Header & Genre Chips */}
      <div className={styles.heroBanner}>
        <h1>Discover Films</h1>
        <p className={styles.tagline}>Explore fresh releases, timeless classics, and tailored categories.</p>
        
        <div className={styles.chipContainer}>
          {GENRES.map((genre) => (
            <button
              key={genre.id}
              className={`${styles.chip} ${selectedGenre === genre.id ? styles.activeChip : ''}`}
              onClick={() => setSelectedGenre(genre.id)}
            >
              {genre.name}
            </button>
          ))}
        </div>
      </div>

      {/* Rows Container */}
      <div className={styles.rowsContainer}>
        {selectedGenre !== 'all' ? (
          <div className={styles.section}>
            <h2 className={styles.sectionTitle}>
              {GENRES.find(g => g.id === selectedGenre)?.name} Movies
            </h2>
            {genreResults.length > 0 ? (
              <MovieGrid movies={genreResults} />
            ) : (
              <p className={styles.state}>No movies found for this genre.</p>
            )}
          </div>
        ) : (
          <>
            <TakeTwoCallout />
            {renderCarousel('Newly Released (2026)', nowPlaying, nowPlayingRef)}
            {renderCarousel('Trending This Week', trending, trendingRef)}
            {renderCarousel('Popular All-Time', popular, popularRef)}
            {renderCarousel('Critically Acclaimed Top Rated', topRated, topRatedRef)}
          </>
        )}
      </div>
    </div>
  );
}