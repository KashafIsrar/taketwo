import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import { getUserLists, createList } from '../services/api';
import * as movieService from '../services/movieService';

// Sub-component to fetch and display individual movie details from TMDB ID
function MovieListItemCard({ tmdbId }) {
  const [movie, setMovie] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;
    movieService.getMovieDetails(tmdbId)
      .then((data) => { if (!cancelled) setMovie(data); })
      .catch(() => {})
      .finally(() => { if (!cancelled) setLoading(false); });
    return () => { cancelled = true; };
  }, [tmdbId]);

  if (loading) {
    return <div style={{ padding: '0.75rem', background: '#14181c', borderRadius: '6px', color: '#888' }}>Loading movie details...</div>;
  }

  if (!movie) {
    return <div style={{ padding: '0.75rem', background: '#14181c', borderRadius: '6px', color: '#888' }}>Movie ID: {tmdbId}</div>;
  }

  const year = movie.releaseDate ? new Date(movie.releaseDate).getFullYear() : null;

  return (
    <Link
      to={`/movie/${tmdbId}`}
      style={{
        display: 'flex',
        gap: '1rem',
        padding: '0.75rem',
        background: '#14181c',
        borderRadius: '6px',
        textDecoration: 'none',
        color: '#fff',
        alignItems: 'center',
        border: '1px solid #242c34',
        transition: 'transform 0.15s ease-in-out',
      }}
    >
      {movie.posterUrl ? (
        <img src={movie.posterUrl} alt={movie.title} style={{ width: '50px', height: '75px', borderRadius: '4px', objectFit: 'cover' }} />
      ) : (
        <div style={{ width: '50px', height: '75px', borderRadius: '4px', background: '#2c3440' }} />
      )}
      <div>
        <h4 style={{ margin: 0, fontSize: '1.1rem', color: '#fff' }}>{movie.title}</h4>
        <p style={{ margin: '0.25rem 0 0 0', fontSize: '0.85rem', color: '#888' }}>
          {[year, movie.voteAverage ? `★ ${movie.voteAverage.toFixed(1)}` : null].filter(Boolean).join(' · ')}
        </p>
      </div>
    </Link>
  );
}

export default function ListsPage() {
  const { user, isAuthenticated } = useAuth();
  const [lists, setLists] = useState([]);
  const [selectedListId, setSelectedListId] = useState(null);
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [isPrivate, setIsPrivate] = useState(false);
  const [loading, setLoading] = useState(true);

  const activeUserId = user?.id || user?.userId || localStorage.getItem('userId');

  useEffect(() => {
    if (!isAuthenticated || !activeUserId) {
      setLoading(false);
      return;
    }

    getUserLists(activeUserId)
      .then((data) => {
        setLists(data);
        setLoading(false);
      })
      .catch((err) => {
        console.error('Failed to load lists', err);
        setLoading(false);
      });
  }, [isAuthenticated, activeUserId]);

  const handleCreateList = async (e) => {
    e.preventDefault();
    if (!title.trim()) return;

    try {
      const newList = await createList({ title, description, isPrivate });
      setLists([newList, ...lists]);
      setTitle('');
      setDescription('');
      setIsPrivate(false);
    } catch (err) {
      console.error('Failed to create list', err);
    }
  };

  const selectedList = lists.find((l) => l.id === selectedListId);

  if (loading) return <div style={{ color: '#fff', padding: '2rem' }}>Loading lists...</div>;

  return (
    <div style={{ padding: '2rem', color: '#fff', maxWidth: '800px', margin: '0 auto' }}>
      <h1>My Movie Lists</h1>

      {selectedList ? (
        <div style={{ background: '#1c2228', padding: '1.5rem', borderRadius: '8px', marginBottom: '2rem' }}>
          <button
            onClick={() => setSelectedListId(null)}
            style={{
              padding: '0.4rem 0.8rem',
              borderRadius: '4px',
              border: 'none',
              background: '#333',
              color: '#fff',
              cursor: 'pointer',
              marginBottom: '1rem',
            }}
          >
            ← Back to All Lists
          </button>
          <h2>{selectedList.title} {selectedList.isPrivate && <span style={{ fontSize: '0.8rem', color: '#888' }}>(Private)</span>}</h2>
          {selectedList.description && <p style={{ color: '#aaa', margin: '0.5rem 0' }}>{selectedList.description}</p>}
          
          <h3 style={{ marginTop: '1.5rem', borderBottom: '1px solid #333', paddingBottom: '0.5rem' }}>Movies in List</h3>
          {!selectedList.items || selectedList.items.length === 0 ? (
            <p style={{ color: '#888', marginTop: '1rem' }}>No movies added to this list yet.</p>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem', marginTop: '1rem' }}>
              {selectedList.items.map((item, index) => {
                const tmdbId = item.movie?.tmdbId || item.tmdbId;
                return <MovieListItemCard key={item.id || index} tmdbId={tmdbId} />;
              })}
            </div>
          )}
        </div>
      ) : (
        <>
          <form
            onSubmit={handleCreateList}
            style={{
              marginBottom: '2rem',
              display: 'flex',
              flexDirection: 'column',
              gap: '1rem',
              background: '#1c2228',
              padding: '1.5rem',
              borderRadius: '8px',
            }}
          >
            <h3>Create New List</h3>
            <input
              type="text"
              placeholder="List Title (e.g., Favorite Sci-Fi)"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              style={{
                padding: '0.5rem',
                borderRadius: '4px',
                border: '1px solid #333',
                background: '#14181c',
                color: '#fff',
              }}
              required
            />
            <textarea
              placeholder="Description"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              style={{
                padding: '0.5rem',
                borderRadius: '4px',
                border: '1px solid #333',
                background: '#14181c',
                color: '#fff',
                minHeight: '60px',
              }}
            />
            <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', cursor: 'pointer' }}>
              <input
                type="checkbox"
                checked={isPrivate}
                onChange={(e) => setIsPrivate(e.target.checked)}
              />
              Make list private
            </label>
            <button
              type="submit"
              style={{
                padding: '0.6rem',
                borderRadius: '4px',
                border: 'none',
                background: '#00e054',
                color: '#14181c',
                fontWeight: 'bold',
                cursor: 'pointer',
              }}
            >
              Create List
            </button>
          </form>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            {lists.length === 0 ? (
              <p>No lists created yet.</p>
            ) : (
              lists.map((list) => (
                <div
                  key={list.id}
                  onClick={() => setSelectedListId(list.id)}
                  style={{
                    padding: '1rem',
                    background: '#1c2228',
                    borderRadius: '8px',
                    cursor: 'pointer',
                    border: '1px solid #333',
                  }}
                >
                  <h3>
                    {list.title} {list.isPrivate && <span style={{ fontSize: '0.8rem', color: '#888' }}>(Private)</span>}
                  </h3>
                  {list.description && <p style={{ color: '#aaa', margin: '0.5rem 0' }}>{list.description}</p>}
                  <span style={{ fontSize: '0.85rem', color: '#00e054' }}>
                    {list.items?.length || 0} items — Click to open
                  </span>
                </div>
              ))
            )}
          </div>
        </>
      )}
    </div>
  );
}