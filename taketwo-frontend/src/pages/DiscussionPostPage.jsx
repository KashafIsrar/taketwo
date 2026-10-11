import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { 
  getDiscussionPost, 
  getTopLevelComments, 
  getReplies, 
  addDiscussionComment, 
  voteOnPost, 
  voteOnComment,
  updateDiscussionPost 
} from '../services/api';
import SpoilerText from '../components/SpoilerText';
import VoteControls from '../components/VoteControls';
import { useAuth } from '../context/AuthContext.jsx';

function EditPostForm({ post, onSaved, onCancel }) {
  const [title, setTitle] = useState(post.title);
  const [body, setBody] = useState(post.body);
  const [isSpoiler, setIsSpoiler] = useState(post.isSpoiler);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  async function handleSubmit(e) {
    e.preventDefault();
    if (!title.trim() || !body.trim()) return;
    setSaving(true);
    setError('');
    try {
      const updated = await updateDiscussionPost(post.postId, { title: title.trim(), body: body.trim(), isSpoiler });
      onSaved(updated);
    } catch (err) {
      setError(err.response?.data?.message || 'Could not save changes.');
    } finally {
      setSaving(false);
    }
  }

  return (
    <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
      <input
        value={title}
        onChange={(e) => setTitle(e.target.value)}
        maxLength={200}
        style={{ padding: '0.6rem', background: '#14181c', border: '1px solid #2c3440', borderRadius: '4px', color: '#fff' }}
      />
      <textarea
        value={body}
        onChange={(e) => setBody(e.target.value)}
        rows={5}
        style={{ padding: '0.6rem', background: '#14181c', border: '1px solid #2c3440', borderRadius: '4px', color: '#fff', resize: 'vertical' }}
      />
      <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: '#fff', fontSize: '0.85rem' }}>
        <input type="checkbox" checked={isSpoiler} onChange={(e) => setIsSpoiler(e.target.checked)} />
        Contains spoilers
      </label>
      {error && <p style={{ color: '#ff4e4e', fontSize: '0.85rem', margin: 0 }}>{error}</p>}
      <div style={{ display: 'flex', gap: '0.5rem' }}>
        <button type="submit" disabled={saving} style={{ background: '#00e054', border: 'none', color: '#14181c', padding: '0.5rem 1.2rem', borderRadius: '4px', fontWeight: 'bold', cursor: 'pointer' }}>
          {saving ? 'Saving...' : 'Save'}
        </button>
        <button type="button" onClick={onCancel} style={{ background: 'none', border: '1px solid #2c3440', color: '#9ab', padding: '0.5rem 1.2rem', borderRadius: '4px', cursor: 'pointer' }}>
          Cancel
        </button>
      </div>
    </form>
  );
}

function ReplyThread({ comment, onVote }) {
  const [replies, setReplies] = useState([]);
  const [page, setPage] = useState(0);
  const [loadedCount, setLoadedCount] = useState(0);
  const [showReplyForm, setShowReplyForm] = useState(false);
  const [replyText, setReplyText] = useState('');
  const [submitting, setSubmitting] = useState(false);

  async function loadMore() {
    const batch = await getReplies(comment.commentId, page, 5);
    setReplies((prev) => [...prev, ...batch]);
    setLoadedCount((prev) => prev + batch.length);
    setPage((prev) => prev + 1);
  }

  async function handleReplySubmit(e) {
    e.preventDefault();
    if (!replyText.trim()) return;
    setSubmitting(true);
    try {
      const newReply = await addDiscussionComment(comment.postId, { body: replyText.trim(), isSpoiler: false, parentCommentId: comment.commentId });
      setReplies((prev) => [...prev, newReply]);
      setLoadedCount((prev) => prev + 1);
      setReplyText('');
      setShowReplyForm(false);
    } catch (err) {
      console.error('Failed to post reply', err);
    } finally {
      setSubmitting(false);
    }
  }

  const remaining = comment.replyCount - loadedCount;

  return (
    <div style={{ marginLeft: '2rem', marginTop: '0.5rem', display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
      {replies.map((r) => (
        <div key={r.commentId} style={{ display: 'flex', gap: '0.6rem' }}>
          <VoteControls score={r.score} myVote={r.myVote} onVote={(type) => onVote(r.commentId, type)} size="small" />
          <div style={{ flex: 1 }}>
            <span style={{ color: '#9ab', fontSize: '0.8rem', fontWeight: 'bold' }}>@{r.user.username}</span>
            <SpoilerText text={r.body} isSpoiler={r.isSpoiler} style={{ color: '#c1c8d0', fontSize: '0.85rem', margin: '0.1rem 0 0' }} />
          </div>
        </div>
      ))}

      {remaining > 0 && (
        <button onClick={loadMore} style={{ background: 'none', border: 'none', color: '#00e054', fontSize: '0.8rem', cursor: 'pointer', textAlign: 'left', padding: 0 }}>
          View {remaining} more {remaining === 1 ? 'reply' : 'replies'}
        </button>
      )}

      {showReplyForm ? (
        <form onSubmit={handleReplySubmit} style={{ display: 'flex', gap: '0.5rem' }}>
          <input
            value={replyText}
            onChange={(e) => setReplyText(e.target.value)}
            placeholder="Write a reply..."
            style={{ flex: 1, padding: '0.4rem 0.6rem', background: '#14181c', border: '1px solid #2c3440', borderRadius: '4px', color: '#fff', fontSize: '0.85rem' }}
          />
          <button type="submit" disabled={submitting || !replyText.trim()} style={{ background: '#00e054', border: 'none', color: '#14181c', padding: '0.4rem 0.8rem', borderRadius: '4px', fontWeight: 'bold', fontSize: '0.8rem', cursor: 'pointer' }}>
            Reply
          </button>
        </form>
      ) : (
        <button onClick={() => setShowReplyForm(true)} style={{ background: 'none', border: 'none', color: '#9ab', fontSize: '0.8rem', cursor: 'pointer', textAlign: 'left', padding: 0 }}>
          Reply
        </button>
      )}
    </div>
  );
}

export default function DiscussionPostPage() {
  const { postId } = useParams();
  const { user } = useAuth();
  const currentUserId = user?.id || user?.userId || localStorage.getItem('userId');

  const [post, setPost] = useState(null);
  const [comments, setComments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [commentText, setCommentText] = useState('');
  const [commentSpoiler, setCommentSpoiler] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [editing, setEditing] = useState(false);

  function loadAll() {
    setLoading(true);
    Promise.all([getDiscussionPost(postId), getTopLevelComments(postId)])
      .then(([postData, commentData]) => {
        setPost(postData);
        setComments(commentData);
      })
      .finally(() => setLoading(false));
  }

  useEffect(() => { loadAll(); }, [postId]);

  async function handlePostVote(type) {
    const result = await voteOnPost(postId, type);
    setPost((prev) => ({ ...prev, score: result.score, myVote: result.myVote }));
  }

  async function handleCommentVote(commentId, type) {
    const result = await voteOnComment(commentId, type);
    setComments((prev) => prev.map((c) => (c.commentId === commentId ? { ...c, score: result.score, myVote: result.myVote } : c)));
  }

  async function handleTopLevelSubmit(e) {
    e.preventDefault();
    if (!commentText.trim()) return;
    setSubmitting(true);
    try {
      const newComment = await addDiscussionComment(postId, { body: commentText.trim(), isSpoiler: commentSpoiler, parentCommentId: null });
      setComments((prev) => [...prev, newComment]);
      setCommentText('');
      setCommentSpoiler(false);
    } catch (err) {
      console.error('Failed to post comment', err);
    } finally {
      setSubmitting(false);
    }
  }

  if (loading || !post) return <div style={{ padding: '2rem', color: '#9ab' }}>Loading...</div>;

  const isAuthor = String(post.user.userId) === String(currentUserId);

  return (
    <div style={{ maxWidth: '700px', margin: '0 auto', padding: '2rem 1.5rem', color: '#fff' }}>
      <Link to={`/discussions/movie/${post.movie.tmdbId}`} style={{ color: '#9ab', textDecoration: 'none', fontSize: '0.85rem' }}>&larr; Back to discussion</Link>

      <div style={{ background: '#1c2228', border: '1px solid #2c3440', borderRadius: '8px', padding: '1.5rem', margin: '1rem 0 1.5rem', display: 'flex', gap: '1rem' }}>
        <VoteControls score={post.score} myVote={post.myVote} onVote={handlePostVote} />
        <div style={{ flex: 1 }}>
          {editing ? (
            <EditPostForm
              post={post}
              onSaved={(updated) => { setPost(updated); setEditing(false); }}
              onCancel={() => setEditing(false)}
            />
          ) : (
            <>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: '1rem' }}>
                <h1 style={{ fontSize: '1.4rem', margin: '0 0 0.25rem' }}>{post.title}</h1>
                {isAuthor && (
                  <button
                    onClick={() => setEditing(true)}
                    style={{ background: 'none', border: '1px solid #2c3440', color: '#9ab', padding: '0.3rem 0.8rem', borderRadius: '4px', cursor: 'pointer', fontSize: '0.8rem', flexShrink: 0 }}
                  >
                    Edit
                  </button>
                )}
              </div>
              <p style={{ color: '#667', fontSize: '0.8rem', margin: '0 0 0.75rem' }}>
                @{post.user.username}{post.updatedAt && ' - edited'}
              </p>
              <SpoilerText text={post.body} isSpoiler={post.isSpoiler} style={{ color: '#c1c8d0', margin: 0 }} />
            </>
          )}
        </div>
      </div>

      <form onSubmit={handleTopLevelSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem', marginBottom: '1.5rem' }}>
        <textarea
          value={commentText}
          onChange={(e) => setCommentText(e.target.value)}
          placeholder="Add a comment..."
          rows={2}
          style={{ padding: '0.6rem', background: '#1c2228', border: '1px solid #2c3440', borderRadius: '4px', color: '#fff', resize: 'vertical' }}
        />
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <label style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', color: '#9ab', fontSize: '0.8rem' }}>
            <input type="checkbox" checked={commentSpoiler} onChange={(e) => setCommentSpoiler(e.target.checked)} />
            Contains spoilers
          </label>
          <button type="submit" disabled={submitting || !commentText.trim()} style={{ background: '#00e054', border: 'none', color: '#14181c', padding: '0.5rem 1.2rem', borderRadius: '4px', fontWeight: 'bold', cursor: 'pointer' }}>
            Comment
          </button>
        </div>
      </form>

      <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
        {comments.map((c) => (
          <div key={c.commentId}>
            <div style={{ display: 'flex', gap: '0.75rem' }}>
              <VoteControls score={c.score} myVote={c.myVote} onVote={(type) => handleCommentVote(c.commentId, type)} />
              <div style={{ flex: 1 }}>
                <span style={{ color: '#fff', fontSize: '0.85rem', fontWeight: 'bold' }}>@{c.user.username}</span>
                <SpoilerText text={c.body} isSpoiler={c.isSpoiler} style={{ color: '#c1c8d0', fontSize: '0.9rem', margin: '0.1rem 0 0' }} />
              </div>
            </div>
            <ReplyThread comment={c} onVote={handleCommentVote} />
          </div>
        ))}
      </div>
    </div>
  );
}