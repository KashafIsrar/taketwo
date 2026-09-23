import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import { toggleFollow } from '../services/api';
import axios from 'axios';

export default function MembersPage() {
  const { user, isAuthenticated } = useAuth();
  const [query, setQuery] = useState('');
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const activeUserId = user?.id || user?.userId || localStorage.getItem('userId');

  const handleSearch = async (e) => {
    e.preventDefault();
    if (!query.trim()) return;

    setLoading(true);
    setError('');

    try {
      const response = await axios.get(`/api/users/search?q=${encodeURIComponent(query.trim())}`);
      setUsers(response.data);
    } catch (err) {
      console.error('Failed to search users', err);
      setError('Failed to fetch users.');
    } finally {
      setLoading(false);
    }
  };

  const handleToggleFollow = async (targetUserId) => {
    try {
      await toggleFollow(targetUserId);
      setUsers((prev) =>
        prev.map((u) => (u.id === targetUserId ? { ...u, isFollowing: !u.isFollowing } : u))
      );
    } catch (err) {
      console.error('Failed to update follow status', err);
    }
  };

  return (
    <div style={{ padding: '2rem', color: '#fff', maxWidth: '800px', margin: '0 auto' }}>
      <h1>Find Members</h1>

      <form onSubmit={handleSearch} style={{ display: 'flex', gap: '0.5rem', marginBottom: '2rem' }}>
        <input
          type="search"
          placeholder="Search members by username or name..."
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          style={{
            flex: 1,
            padding: '0.75rem',
            borderRadius: '4px',
            border: '1px solid #333',
            background: '#14181c',
            color: '#fff',
          }}
        />
        <button
          type="submit"
          style={{
            padding: '0.75rem 1.5rem',
            borderRadius: '4px',
            border: 'none',
            background: '#00e054',
            color: '#14181c',
            fontWeight: 'bold',
            cursor: 'pointer',
          }}
        >
          Search
        </button>
      </form>

      {loading && <p style={{ color: '#888' }}>Searching...</p>}
      {error && <p style={{ color: '#ff4e4e' }}>{error}</p>}

      <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
        {users.map((member) => (
          <div
            key={member.id}
            style={{
              padding: '1rem',
              background: '#1c2228',
              borderRadius: '8px',
              display: 'flex',
              justify: 'space-between',
              alignItems: 'center',
            }}
          >
            <div>
              <Link
                to={`/user/${member.id}`}
                style={{ color: '#fff', textDecoration: 'none', fontWeight: 'bold', fontSize: '1.1rem' }}
              >
                {member.displayName || member.username}
              </Link>
              <p style={{ margin: '0.25rem 0 0 0', color: '#888', fontSize: '0.85rem' }}>@{member.username}</p>
            </div>

            {isAuthenticated && activeUserId !== member.id && (
              <button
                onClick={() => handleToggleFollow(member.id)}
                style={{
                  padding: '0.5rem 1rem',
                  borderRadius: '4px',
                  border: 'none',
                  background: member.isFollowing ? '#333' : '#00e054',
                  color: member.isFollowing ? '#fff' : '#14181c',
                  fontWeight: 'bold',
                  cursor: 'pointer',
                }}
              >
                {member.isFollowing ? 'Following' : 'Follow'}
              </button>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}