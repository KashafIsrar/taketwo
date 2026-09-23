import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import LogMovieModal from '../components/LogMovieModal.jsx';
import * as movieService from '../services/movieService';
import { getUserLists, addMovieToList } from '../services/api';
import styles from './MovieDetailsPage.module.css';

export default function MovieDetailsPage() {
  const { tmdbId } = useParams();
  const { isAuthenticated, user } = useAuth();
  const navigate = useNavigate();

  const [movie, setMovie] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [logModalOpen, setLogModalOpen] = useState(false);
  const [loggedConfirmation, setLoggedConfirmation] = useState(false);

  const [onWatchlist, setOnWatchlist] = useState(null);
  const [watchlistBusy, setWatchlistBusy] = useState(false);

  // Custom List States
  const [userLists, setUserLists] = useState([]);
  const [selectedListId, setSelectedListId] = useState('');
  const [addListStatus, setAddListStatus] = useState('');

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError('');

    movieService.getMovieDetails(tmdbId)
      .then((data) => { if (!cancelled) setMovie(data); })
      .catch(() => { if (!cancelled) setError('Could not load this movie.'); })
      .finally(() => { if (!cancelled) setLoading(false); });

    return () => { cancelled = true; };
  }, [tmdbId]);

  useEffect(() => {
    if (!isAuthenticated) return;
    let cancelled = false;
    movieService.getWatchlistStatus(tmdbId)
      .then((status) => { if (!cancelled) setOnWatchlist(status); })
      .catch(() => {});
    return () => { cancelled = true; };
  }, [tmdbId, isAuthenticated]);

  // Load custom lists for logged-in user
  useEffect(() => {
    if (!isAuthenticated) return;
    let cancelled = false;
    
    // Fallback to get user ID regardless of context field naming
    const activeUserId = user?.id || user?.userId || localStorage.getItem('userId');
    if (!activeUserId) return;

    getUserLists(activeUserId)
      .then((lists) => { if (!cancelled) setUserLists(lists); })
      .catch((err) => console.error('Failed to fetch user lists:', err));
      
    return () => { cancelled = true; };
  }, [isAuthenticated, user]);

  function requireAuth() {
    if (!isAuthenticated) {
      navigate('/login');
      return false;
    }
    return true;
  }

  function handleLogClick() {
    if (requireAuth()) setLogModalOpen(true);
  }

  async function handleWatchlistToggle() {
    if (!requireAuth()) return;
    setWatchlistBusy(true);
    try {
      const result = await movieService.toggleWatchlist(movie.tmdbId);
      setOnWatchlist(result.onWatchlist);
    } catch {
      // Leave state as-is
    } finally {
      setWatchlistBusy(false);
    }
  }

  async function handleAddToList() {
    if (!requireAuth() || !selectedListId) return;
    try {
      await addMovieToList(selectedListId, movie.tmdbId);
      setAddListStatus('Added to list!');
      setTimeout(() => setAddListStatus(''), 3000);
    } catch {
      setAddListStatus('Failed to add.');
    }
  }

  if (loading) return <div className={styles.page}><p className={styles.state}>Loading…</p></div>;
  if (error || !movie) return <div className={styles.page}><p className={styles.state}>{error || 'Movie not found.'}</p></div>;

  const year = movie.releaseDate ? new Date(movie.releaseDate).getFullYear() : null;

  return (
    <div className={styles.hero}>
      <div className={styles.page}>
        <div className={styles.layout}>
          <div className={styles.posterFrame}>
            <div className={styles.sprockets} />
            {movie.posterUrl ? (
              <img className={styles.poster} src={movie.posterUrl} alt={movie.title} />
            ) : (
              <div className={styles.poster} />
            )}
            <div className={styles.sprockets} />
          </div>

          <div>
            <h1 className={styles.title}>{movie.title}</h1>
            <p className={styles.meta}>
              {[year, movie.runtimeMinutes ? `${movie.runtimeMinutes} min` : null, movie.voteAverage ? `★ ${movie.voteAverage.toFixed(1)} community` : null]
                .filter(Boolean)
                .join(' · ')}
            </p>

            {movie.genres?.length > 0 && (
              <div className={styles.genres}>
                {movie.genres.map((g) => <span key={g} className={styles.genreTag}>{g}</span>)}
              </div>
            )}

            <p className={styles.overview}>{movie.overview}</p>

            <div className={styles.actions}>
              <button className={styles.btnPrimary} onClick={handleLogClick}>Log film</button>
              <button
                className={`${styles.btnSecondary} ${onWatchlist ? styles.active : ''}`}
                onClick={handleWatchlistToggle}
                disabled={watchlistBusy}
              >
                {onWatchlist ? '✓ On watchlist' : '+ Watchlist'}
              </button>

              {/* Add to List Controls */}
              {isAuthenticated && (
                <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
                  <select
                    value={selectedListId}
                    onChange={(e) => setSelectedListId(e.target.value)}
                    style={{ padding: '0.5rem', borderRadius: '4px', background: '#14181c', color: '#fff', border: '1px solid #333' }}
                  >
                    <option value="">
                      {userLists.length > 0 ? '+ Add to List...' : 'No lists found'}
                    </option>
                    {userLists.map((list) => (
                      <option key={list.id} value={list.id}>
                        {list.title}
                      </option>
                    ))}
                  </select>
                  <button
                    className={styles.btnSecondary}
                    onClick={handleAddToList}
                    disabled={!selectedListId}
                  >
                    Add
                  </button>
                </div>
              )}
            </div>

            {addListStatus && <p className={styles.confirmation}>{addListStatus}</p>}
            {loggedConfirmation && <p className={styles.confirmation}>Logged — check your diary.</p>}
          </div>
        </div>

        <LogMovieModal
          movie={movie}
          isOpen={logModalOpen}
          onClose={() => setLogModalOpen(false)}
          onLogged={() => setLoggedConfirmation(true)}
        />
      </div>
    </div>
  );
}