// pages/ConversationPage.jsx
import { useEffect, useRef, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import { getInbox, sendMessage, markConversationRead } from '../services/api';
import useMessagePolling from '../hooks/useMessagePolling';
import api from '../services/api';

const WALLPAPERS = [
  { name: 'Default Dark', value: '#14181c', type: 'color' },
  { name: 'Cinematic Noir', value: 'radial-gradient(circle at center, #1c2630 0%, #0e1217 100%)', type: 'gradient' },
  { name: 'Neon Emerald', value: 'radial-gradient(circle at top right, #003b1f 0%, #14181c 70%)', type: 'gradient' },
  { name: 'Deep Midnight', value: 'linear-gradient(135deg, #0d1117 0%, #161b22 100%)', type: 'gradient' },
];

export default function ConversationPage() {
  const { conversationId } = useParams();
  const { user } = useAuth();
  const currentUserId = user?.id || user?.userId || localStorage.getItem('userId');

  const { messages, loading, appendLocalMessage } = useMessagePolling(conversationId);
  const [otherUser, setOtherUser] = useState(null);
  const [draft, setDraft] = useState('');
  const [sending, setSending] = useState(false);
  const bottomRef = useRef(null);

  // Blocking & Settings States
  const [isBlocked, setIsBlocked] = useState(false);
  const [showDropdown, setShowDropdown] = useState(false);

  // Custom Wallpaper State per conversation
  const [wallpapers, setWallpapers] = useState(() => {
    try {
      return JSON.parse(localStorage.getItem('chat_wallpapers') || '{}');
    } catch {
      return {};
    }
  });
  const [showWallpaperModal, setShowWallpaperModal] = useState(false);
  const [customWallpaperInput, setCustomWallpaperInput] = useState('');

  // Fetch inbox details and directly cross-reference the blocked list endpoint
  useEffect(() => {
    let active = true;

    async function loadChatData() {
      try {
        const [inboxList, blockedRes] = await Promise.all([
          getInbox(),
          api.get('/users/blocked').catch(() => ({ data: [] }))
        ]);

        if (!active) return;

        const match = inboxList.find((c) => c.conversationId === conversationId);
        if (match && match.otherUser) {
          setOtherUser(match.otherUser);

          const targetId = match.otherUser.userId || match.otherUser.id;
          const blockedUsers = blockedRes.data || blockedRes;
          
          // Cross-reference user's ID against the blocked list array from backend
          const isUserBlocked = Array.isArray(blockedUsers) && blockedUsers.some(
            (u) => String(u.id || u.userId) === String(targetId)
          );

          setIsBlocked(isUserBlocked || Boolean(match.isBlocked || match.otherUser?.isBlocked));
        }
      } catch (err) {
        console.error('Failed to load conversation details', err);
      }
    }

    loadChatData();
    return () => { active = false; };
  }, [conversationId]);

  useEffect(() => {
    if (conversationId) markConversationRead(conversationId).catch(() => {});
  }, [conversationId, messages.length]);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages.length]);

  async function handleSend(e) {
    e.preventDefault();
    if (!draft.trim() || sending || isBlocked) return;
    setSending(true);
    try {
      const sent = await sendMessage(conversationId, draft.trim());
      appendLocalMessage(sent);
      setDraft('');
    } catch (err) {
      console.error('Failed to send message', err);
    } finally {
      setSending(false);
    }
  }

  const handleToggleBlock = async () => {
    if (!otherUser) return;
    const targetId = otherUser.userId || otherUser.id;
    try {
      if (isBlocked) {
        await api.post(`/users/${targetId}/unblock`);
        setIsBlocked(false);
      } else {
        await api.post(`/users/${targetId}/block`);
        setIsBlocked(true);
      }
    } catch (err) {
      console.error('Failed to update block status', err);
      setIsBlocked(prev => !prev);
    }
    setShowDropdown(false);
  };

  const handleSetWallpaper = (bgValue) => {
    const updated = { ...wallpapers, [conversationId]: bgValue };
    setWallpapers(updated);
    localStorage.setItem('chat_wallpapers', JSON.stringify(updated));
    setShowWallpaperModal(false);
  };

  const currentWallpaper = (conversationId && wallpapers[conversationId]) || '#14181c';

  return (
    <div style={{ 
      maxWidth: '740px', 
      margin: '0 auto', 
      padding: '1.5rem', 
      display: 'flex', 
      flexDirection: 'column', 
      height: 'calc(100vh - 73px)', 
      background: currentWallpaper,
      backgroundSize: 'cover',
      backgroundPosition: 'center',
      borderRadius: '8px',
      position: 'relative'
    }}>
      
      {/* Header with Avatar, Profile Link & Options Menu */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', paddingBottom: '1rem', borderBottom: '1px solid #2c3440', marginBottom: '1rem', background: 'rgba(24, 31, 38, 0.4)', padding: '0.75rem 1rem', borderRadius: '8px', backdropFilter: 'blur(4px)' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.85rem' }}>
          <Link to="/messages" style={{ color: '#9ab', textDecoration: 'none', fontSize: '1.2rem' }}>&larr;</Link>
          {otherUser && (
            <Link to={`/user/${otherUser.userId || otherUser.id}`} style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', textDecoration: 'none', color: '#fff' }}>
              <div style={{ width: '36px', height: '36px', borderRadius: '50%', background: '#2c3440', overflow: 'hidden', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                {otherUser.profilePictureUrl ? (
                  <img src={otherUser.profilePictureUrl} alt="" style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                ) : (
                  <span style={{ fontWeight: 'bold', fontSize: '0.85rem', color: '#9ab' }}>{(otherUser.username || '?').charAt(0).toUpperCase()}</span>
                )}
              </div>
              <div>
                <span style={{ fontWeight: 'bold', display: 'block', fontSize: '0.95rem' }}>@{otherUser.username}</span>
                <span style={{ fontSize: '0.72rem', color: isBlocked ? '#ff4e4e' : '#00e054' }}>{isBlocked ? 'Blocked' : 'View Profile'}</span>
              </div>
            </Link>
          )}
        </div>

        {/* Action Controls */}
        <div style={{ position: 'relative', display: 'flex', gap: '0.5rem' }}>
          <button 
            onClick={() => setShowWallpaperModal(true)}
            style={{ background: '#2c3440', border: '1px solid #3c4450', color: '#fff', padding: '0.35rem 0.7rem', borderRadius: '4px', cursor: 'pointer', fontSize: '0.8rem' }}
          >
            Wallpaper
          </button>
          <button 
            onClick={() => setShowDropdown(prev => !prev)}
            style={{ background: '#2c3440', border: '1px solid #3c4450', color: '#fff', padding: '0.35rem 0.7rem', borderRadius: '4px', cursor: 'pointer', fontSize: '0.9rem', fontWeight: 'bold' }}
          >
            &#8942;
          </button>

          {showDropdown && (
            <div style={{ position: 'absolute', right: 0, top: '110%', background: '#1c242c', border: '1px solid #2c3440', borderRadius: '6px', width: '140px', boxShadow: '0 6px 20px rgba(0,0,0,0.5)', zIndex: 50, overflow: 'hidden' }}>
              <button 
                onClick={handleToggleBlock}
                style={{ width: '100%', textAlign: 'left', background: 'none', border: 'none', color: isBlocked ? '#00e054' : '#ff4e4e', padding: '0.65rem 1rem', cursor: 'pointer', fontSize: '0.82rem', fontWeight: '600' }}
              >
                {isBlocked ? 'Unblock User' : 'Block User'}
              </button>
            </div>
          )}
        </div>
      </div>

      {/* Message Stream */}
      <div style={{ flex: 1, overflowY: 'auto', display: 'flex', flexDirection: 'column', gap: '0.6rem', paddingRight: '0.25rem' }}>
        {loading ? (
          <p style={{ color: '#9ab', textAlign: 'center' }}>Loading...</p>
        ) : messages.length === 0 ? (
          <p style={{ color: '#9ab', textAlign: 'center', marginTop: '3rem' }}>No messages yet. Say hello.</p>
        ) : (
          messages.map((m) => {
            const isMine = String(m.sender?.userId) === String(currentUserId);
            return (
              <div key={m.messageId} style={{ display: 'flex', justifyContent: isMine ? 'flex-end' : 'flex-start' }}>
                <div style={{
                  maxWidth: '70%', background: isMine ? '#00b343' : 'rgba(28, 34, 40, 0.9)',
                  color: '#fff', border: isMine ? 'none' : '1px solid #2c3440',
                  borderRadius: '12px', padding: '0.65rem 0.95rem', fontSize: '0.9rem', wordBreak: 'break-word',
                  boxShadow: '0 2px 6px rgba(0,0,0,0.2)'
                }}>
                  <p style={{ margin: 0, lineHeight: '1.4' }}>{m.content}</p>
                  <span style={{ display: 'block', fontSize: '0.65rem', color: isMine ? 'rgba(255,255,255,0.8)' : '#8a99a8', textAlign: 'right', marginTop: '0.2rem' }}>
                    {m.createdAt ? new Date(m.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : ''}
                  </span>
                </div>
              </div>
            );
          })
        )}
        <div ref={bottomRef} />
      </div>

      {/* Input Box or Block Notice */}
      {isBlocked ? (
        <div style={{ padding: '0.85rem', background: 'rgba(28, 34, 40, 0.95)', textAlign: 'center', borderTop: '1px solid #2c3440', borderRadius: '6px', color: '#ff4e4e', fontSize: '0.85rem', marginTop: '1rem' }}>
          You have blocked this user. Unblock them from the menu to resume conversation.
        </div>
      ) : (
        <form onSubmit={handleSend} style={{ display: 'flex', gap: '0.5rem', marginTop: '1rem', paddingTop: '1rem', borderTop: '1px solid #2c3440', background: 'rgba(24, 31, 38, 0.4)', padding: '0.75rem', borderRadius: '8px', backdropFilter: 'blur(4px)' }}>
          <input
            type="text"
            value={draft}
            onChange={(e) => setDraft(e.target.value)}
            placeholder="Type a message..."
            style={{ flex: 1, padding: '0.7rem 1rem', background: '#1c2228', border: '1px solid #2c3440', borderRadius: '20px', color: '#fff', fontSize: '0.9rem', outline: 'none' }}
          />
          <button
            type="submit"
            disabled={sending || !draft.trim()}
            style={{ background: '#00e054', border: 'none', color: '#14181c', padding: '0 1.4rem', borderRadius: '20px', fontWeight: 'bold', cursor: 'pointer' }}
          >
            Send
          </button>
        </form>
      )}

      {/* Wallpaper Picker Modal */}
      {showWallpaperModal && (
        <div style={{ position: 'absolute', inset: 0, background: 'rgba(0,0,0,0.7)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 100, backdropFilter: 'blur(4px)', borderRadius: '8px' }}>
          <div style={{ background: '#1c242c', border: '1px solid #2c3440', padding: '1.5rem', borderRadius: '8px', width: '100%', maxWidth: '360px', display: 'flex', flexDirection: 'column', gap: '1rem', boxShadow: '0 8px 32px rgba(0,0,0,0.6)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <h3 style={{ margin: 0, fontSize: '1.1rem', color: '#fff' }}>Choose Wallpaper</h3>
              <button onClick={() => setShowWallpaperModal(false)} style={{ background: 'none', border: 'none', color: '#9ab', cursor: 'pointer', fontSize: '1.25rem' }}>&times;</button>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '0.75rem' }}>
              {WALLPAPERS.map((wp, idx) => (
                <div 
                  key={idx}
                  onClick={() => handleSetWallpaper(wp.value)}
                  style={{ 
                    height: '60px', 
                    background: wp.value, 
                    borderRadius: '6px', 
                    border: '2px solid #3c4450', 
                    cursor: 'pointer', 
                    display: 'flex', 
                    alignItems: 'flex-end', 
                    padding: '0.4rem',
                    fontSize: '0.75rem',
                    fontWeight: '600',
                    color: '#fff'
                  }}
                >
                  {wp.name}
                </div>
              ))}
            </div>

            <div style={{ marginTop: '0.25rem' }}>
              <label style={{ display: 'block', fontSize: '0.78rem', color: '#9ab', marginBottom: '0.3rem' }}>Custom Image URL</label>
              <div style={{ display: 'flex', gap: '0.5rem' }}>
                <input 
                  type="text" 
                  placeholder="https://example.com/bg.jpg"
                  value={customWallpaperInput}
                  onChange={(e) => setCustomWallpaperInput(e.target.value)}
                  style={{ flexGrow: 1, padding: '0.45rem', background: '#14181c', border: '1px solid #2c3440', borderRadius: '4px', color: '#fff', fontSize: '0.82rem', outline: 'none' }}
                />
                <button 
                  onClick={() => {
                    if (customWallpaperInput.trim()) {
                      handleSetWallpaper(`url(${customWallpaperInput.trim()})`);
                      setCustomWallpaperInput('');
                    }
                  }}
                  style={{ background: '#00e054', color: '#14181c', border: 'none', padding: '0.45rem 0.8rem', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold', fontSize: '0.82rem' }}
                >
                  Apply
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

    </div>
  );
}