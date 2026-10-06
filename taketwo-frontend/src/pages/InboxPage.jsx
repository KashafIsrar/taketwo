// pages/InboxPage.jsx
import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getInbox } from '../services/api';
import api from '../services/api';

function formatRelativeTime(isoString) {
  if (!isoString) return '';
  const diffMs = Date.now() - new Date(isoString).getTime();
  const mins = Math.floor(diffMs / 60000);
  if (mins < 1) return 'just now';
  if (mins < 60) return `${mins}m ago`;
  const hours = Math.floor(mins / 60);
  if (hours < 24) return `${hours}h ago`;
  const days = Math.floor(hours / 24);
  return `${days}d ago`;
}

export default function InboxPage() {
  const [conversations, setConversations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [filter, setFilter] = useState('all'); // 'all' or 'blocked'

  // Blocked Users Modal State
  const [showBlockedModal, setShowBlockedModal] = useState(false);
  const [blockedUsers, setBlockedUsers] = useState([]);
  const [loadingBlocked, setLoadingBlocked] = useState(false);

  useEffect(() => {
    let cancelled = false;
    getInbox()
      .then((data) => { if (!cancelled) setConversations(data); })
      .catch((err) => { if (!cancelled) setError(err.response?.data?.message || 'Could not load messages.'); })
      .finally(() => { if (!cancelled) setLoading(false); });
    return () => { cancelled = true; };
  }, []);

  const fetchBlockedUsers = () => {
    setLoadingBlocked(true);
    // Ensure this matches your backend route for fetching blocked users list
    api.get('/users/blocked')
      .then(res => {
        setBlockedUsers(Array.isArray(res.data) ? res.data : []);
      })
      .catch(err => {
        console.error('Failed to load blocked users', err);
      })
      .finally(() => setLoadingBlocked(false));
  };

  const handleOpenBlockedModal = () => {
    setShowBlockedModal(true);
    fetchBlockedUsers();
  };

  const handleUnblockUser = async (userId) => {
    try {
      await api.post(`/users/${userId}/unblock`);
      setBlockedUsers(prev => prev.filter(u => (u.id || u.userId) !== userId));
      // Refresh inbox to update states
      const data = await getInbox();
      setConversations(data);
    } catch (err) {
      console.error('Failed to unblock user', err);
      alert('Could not unblock user.');
    }
  };

  if (loading) return <div style={{ padding: '2rem', color: '#9ab' }}>Loading messages...</div>;
  if (error) return <div style={{ padding: '2rem', color: '#ff4e4e' }}>{error}</div>;

  const filteredConversations = conversations.filter(c => {
    const isBlocked = Boolean(c.isBlocked || c.otherUser?.isBlocked);
    if (filter === 'blocked') return isBlocked;
    return !isBlocked;
  });

  return (
    <div style={{ maxWidth: '640px', margin: '0 auto', padding: '2rem 1.5rem', color: '#fff' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
          <h1 style={{ fontSize: '1.75rem', margin: 0 }}>Messages</h1>
          <button
            onClick={handleOpenBlockedModal}
            style={{ background: '#2c3440', border: '1px solid #3c4450', color: '#ff4e4e', padding: '0.35rem 0.75rem', borderRadius: '6px', cursor: 'pointer', fontSize: '0.8rem', fontWeight: '600' }}
          >
            Blocked Users
          </button>
        </div>
        
        {/* Filter Tabs for Active vs Blocked */}
        <div style={{ display: 'flex', background: '#1c2228', padding: '0.25rem', borderRadius: '6px', border: '1px solid #2c3440' }}>
          <button
            onClick={() => setFilter('all')}
            style={{
              background: filter === 'all' ? '#2c3440' : 'transparent',
              border: 'none', color: '#fff', padding: '0.4rem 0.8rem', borderRadius: '4px',
              cursor: 'pointer', fontSize: '0.82rem', fontWeight: '500'
            }}
          >
            Chats
          </button>
          <button
            onClick={() => setFilter('blocked')}
            style={{
              background: filter === 'blocked' ? '#2c3440' : 'transparent',
              border: 'none', color: filter === 'blocked' ? '#ff4e4e' : '#9ab', padding: '0.4rem 0.8rem', borderRadius: '4px',
              cursor: 'pointer', fontSize: '0.82rem', fontWeight: '500'
            }}
          >
            Blocked
          </button>
        </div>
      </div>

      {filteredConversations.length === 0 ? (
        <div style={{ background: '#1c2228', border: '1px solid #2c3440', borderRadius: '8px', padding: '2rem', textAlign: 'center', color: '#9ab' }}>
          {filter === 'blocked' ? 'No blocked users found.' : "No conversations yet. Visit a member's profile to start one."}
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
          {filteredConversations.map((c) => {
            const isBlocked = Boolean(c.isBlocked || c.otherUser?.isBlocked);
            return (
              <Link
                key={c.conversationId}
                to={`/messages/${c.conversationId}`}
                style={{
                  display: 'flex', justifyContent: 'space-between', alignItems: 'center',
                  background: '#1c2228', border: c.unread ? '1px solid #00e054' : '1px solid #2c3440',
                  borderRadius: '8px', padding: '1rem 1.25rem', textDecoration: 'none',
                }}
              >
                <div style={{ overflow: 'hidden' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.2rem' }}>
                    <span style={{ color: '#fff', fontWeight: c.unread ? 'bold' : 'normal' }}>
                      @{c.otherUser?.username || 'Unknown'}
                    </span>
                    {isBlocked && (
                      <span style={{ fontSize: '0.68rem', background: 'rgba(255, 78, 78, 0.15)', color: '#ff4e4e', padding: '0.1rem 0.4rem', borderRadius: '4px', fontWeight: '600' }}>
                        Blocked
                      </span>
                    )}
                  </div>
                  <div style={{
                    color: c.unread ? '#c1c8d0' : '#9ab', fontSize: '0.85rem',
                    whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis', maxWidth: '380px',
                  }}>
                    {isBlocked ? 'Conversation restricted due to block status' : (c.lastMessagePreview || 'No messages yet')}
                  </div>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', flexShrink: 0, marginLeft: '1rem' }}>
                  <span style={{ color: '#667', fontSize: '0.8rem' }}>{formatRelativeTime(c.lastMessageAt)}</span>
                  {c.unread && !isBlocked && <span style={{ width: '8px', height: '8px', borderRadius: '50%', background: '#00e054' }} />}
                </div>
              </Link>
            );
          })}
        </div>
      )}

      {/* Blocked Users Management Modal */}
      {showBlockedModal && (
        <div style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.7)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000, backdropFilter: 'blur(4px)' }}>
          <div style={{ background: '#1c242c', border: '1px solid #2c3440', padding: '1.5rem', borderRadius: '8px', width: '100%', maxWidth: '420px', color: '#fff', boxShadow: '0 8px 32px rgba(0,0,0,0.6)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
              <h3 style={{ margin: 0, fontSize: '1.1rem' }}>Blocked Users List</h3>
              <button onClick={() => setShowBlockedModal(false)} style={{ background: 'none', border: 'none', color: '#9ab', cursor: 'pointer', fontSize: '1.25rem' }}>&times;</button>
            </div>

            {loadingBlocked ? (
              <p style={{ textAlign: 'center', color: '#9ab', padding: '1.5rem' }}>Loading blocked users...</p>
            ) : blockedUsers.length === 0 ? (
              <p style={{ textAlign: 'center', color: '#9ab', padding: '1.5rem' }}>You haven't blocked anyone.</p>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem', maxHeight: '300px', overflowY: 'auto' }}>
                {blockedUsers.map(user => {
                  const uId = user.id || user.userId;
                  return (
                    <div key={uId} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', background: '#14181c', padding: '0.75rem', borderRadius: '6px', border: '1px solid #2c3440' }}>
                      <span style={{ fontWeight: '500', fontSize: '0.9rem' }}>@{user.username || user.displayName}</span>
                      <button 
                        onClick={() => handleUnblockUser(uId)}
                        style={{ background: '#2c3440', border: '1px solid #3c4450', color: '#00e054', padding: '0.35rem 0.7rem', borderRadius: '4px', cursor: 'pointer', fontSize: '0.8rem', fontWeight: 'bold' }}
                      >
                        Unblock
                      </button>
                    </div>
                  );
                })}
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}