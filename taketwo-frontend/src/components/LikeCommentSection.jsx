import { useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api';

export default function LikeCommentSection({ logId }) {
  const [liked, setLiked] = useState(false);
  const [likeCount, setLikeCount] = useState(0);
  const [statusLoaded, setStatusLoaded] = useState(false);
  const [likeBusy, setLikeBusy] = useState(false);

  const [comments, setComments] = useState([]);
  const [commentsOpen, setCommentsOpen] = useState(false);
  const [commentText, setCommentText] = useState('');
  const [posting, setPosting] = useState(false);

  // Load like status lazily on first interaction rather than for every feed
  // item on mount - a feed can have 30 items, and 30 extra requests just to
  // show initial heart state isn't worth it.
  async function ensureStatusLoaded() {
    if (statusLoaded) return;
    try {
      const res = await api.get(`/movies/logs/${logId}/like-status`);
      setLiked(res.data.liked);
      setLikeCount(res.data.likeCount);
    } catch {
      // leave default state
    } finally {
      setStatusLoaded(true);
    }
  }

  async function handleToggleLike() {
    if (likeBusy) return;
    setLikeBusy(true);
    try {
      const res = await api.post(`/movies/logs/${logId}/like`);
      setLiked(res.data.liked);
      setLikeCount(res.data.likeCount);
      setStatusLoaded(true);
    } catch (err) {
      console.error('Failed to toggle like', err);
    } finally {
      setLikeBusy(false);
    }
  }

  async function loadComments() {
    try {
      const res = await api.get(`/movies/logs/${logId}/comments`);
      setComments(Array.isArray(res.data) ? res.data : []);
    } catch (err) {
      console.error('Failed to load comments', err);
    }
  }

  function handleToggleComments() {
    const next = !commentsOpen;
    setCommentsOpen(next);
    if (next && comments.length === 0) loadComments();
  }

  async function handlePostComment(e) {
    e.preventDefault();
    if (!commentText.trim()) return;
    setPosting(true);
    try {
      const res = await api.post(`/movies/logs/${logId}/comments`, { commentText: commentText.trim() });
      setComments((prev) => [...prev, res.data]);
      setCommentText('');
    } catch (err) {
      console.error('Failed to post comment', err);
    } finally {
      setPosting(false);
    }
  }

  return (
    <div style={{ marginTop: '0.75rem', paddingTop: '0.75rem', borderTop: '1px solid #2c3440', display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
      <div style={{ display: 'flex', gap: '1.25rem', alignItems: 'center' }}>
        <button
          onClick={() => { ensureStatusLoaded(); handleToggleLike(); }}
          disabled={likeBusy}
          style={{ background: 'none', border: 'none', cursor: 'pointer', color: liked ? '#00e054' : '#9ab', fontWeight: 'bold', fontSize: '0.9rem', padding: 0 }}
        >
          {liked ? '♥' : '♡'} {likeCount > 0 ? likeCount : ''} {likeCount === 1 ? 'Like' : 'Likes'}
        </button>
        <button
          onClick={handleToggleComments}
          style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#9ab', fontWeight: 'bold', fontSize: '0.9rem', padding: 0 }}
        >
          💬 Comments
        </button>
      </div>

      {commentsOpen && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem', marginTop: '0.25rem' }}>
          {comments.length === 0 && <p style={{ color: '#667', fontSize: '0.85rem', margin: 0 }}>No comments yet.</p>}
          {comments.map((c) => (
            <div key={c.commentId} style={{ fontSize: '0.85rem', color: '#c1c8d0' }}>
              <Link to={`/user/${c.user?.userId}`} style={{ color: '#00e054', fontWeight: 'bold', textDecoration: 'none' }}>
                @{c.user?.username}
              </Link>{' '}
              {c.commentText}
            </div>
          ))}

          <form onSubmit={handlePostComment} style={{ display: 'flex', gap: '0.5rem', marginTop: '0.25rem' }}>
            <input
              type="text"
              value={commentText}
              onChange={(e) => setCommentText(e.target.value)}
              placeholder="Add a comment…"
              style={{ flex: 1, padding: '0.4rem 0.6rem', background: '#14181c', border: '1px solid #2c3440', borderRadius: '4px', color: '#fff', fontSize: '0.85rem' }}
            />
            <button
              type="submit"
              disabled={posting || !commentText.trim()}
              style={{ background: '#00e054', border: 'none', color: '#14181c', padding: '0.4rem 0.8rem', borderRadius: '4px', fontWeight: 'bold', fontSize: '0.85rem', cursor: 'pointer' }}
            >
              Post
            </button>
          </form>
        </div>
      )}
    </div>
  );
}