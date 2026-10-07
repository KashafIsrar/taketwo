import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { getDiscussionPosts, createDiscussionPost, voteOnPost } from '../services/api';
import SpoilerText from '../components/SpoilerText';
import VoteControls from '../components/VoteControls';

function NewPostForm({ tmdbId, onCreated }) {
  const [title, setTitle] = useState('');
  const [body, setBody] = useState('');
  const [isSpoiler, setIsSpoiler] = useState(false);
  const [open, setOpen] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  async function handleSubmit(e) {
    e.preventDefault();
    if (!title.trim() || !body.trim()) return;
    setSubmitting(true);
    setError('');
    try {
      const post = await createDiscussionPost(tmdbId, { title: title.trim(), body: body.trim(), isSpoiler });
      onCreated(post);
      setTitle('');
      setBody('');
      setIsSpoiler(false);
      setOpen(false);
    } catch (err) {
      setError(err.response?.data?.message || 'Could not create post.');
    } finally {
      setSubmitting(false);
    }
  }

  if (!open) {
    return (
      <button onClick={() => setOpen(true)} style={{ background: '#00e054', border: 'none', color: '#14181c', padding: '0.6rem 1.2rem', borderRadius: '6px', fontWeight: 'bold', cursor: 'pointer', marginBottom: '1.5rem' }}>
        New Post
      </button>
    );
  }

  return (
    <form onSubmit={handleSubmit} style={{ background: '#1c2228', border: '1px solid #2c3440', borderRadius: '8px', padding: '1.25rem', marginBottom: '1.5rem', display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
      <input
        value={title}
        onChange={(e) => setTitle(e.target.value)}
        placeholder="Post title"
        style={{ padding: '0.6rem', background: '#14181c', border: '1px solid #2c3440', borderRadius: '4px', color: '#fff' }}
      />
      <textarea
        value={body}
        onChange={(e) => setBody(e.target.value)}
        placeholder="What's on your mind about this film?"
        rows={4}
        style={{ padding: '0.6rem', background: '#14181c', border: '1px solid #2c3440', borderRadius: '4px', color: '#fff', resize: 'vertical' }}
      />
      <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: '#fff', fontSize: '0.85rem' }}>
        <input type="checkbox" checked={isSpoiler} onChange={(e) => setIsSpoiler(e.target.checked)} />
        Contains spoilers
      </label>
      {error && <p style={{ color: '#ff4e4e', fontSize: '0.85rem', margin: 0 }}>{error}</p>}
      <div style={{ display: 'flex', gap: '0.5rem' }}>
        <button type="submit" disabled={submitting} style={{ background: '#00e054', border: 'none', color: '#14181c', padding: '0.5rem 1.2rem', borderRadius: '4px', fontWeight: 'bold', cursor: 'pointer' }}>
          {submitting ? 'Posting...' : 'Post'}
        </button>
        <button type="button" onClick={() => setOpen(false)} style={{ background: 'none', border: '1px solid #2c3440', color: '#9ab', padding: '0.5rem 1.2rem', borderRadius: '4px', cursor: 'pointer' }}>
          Cancel
        </button>
      </div>
    </form>
  );
}

export default function DiscussionRoomPage() {
  const { tmdbId } = useParams();
  const [posts, setPosts] = useState([]);
  const [sort, setSort] = useState('new');
  const [loading, setLoading] = useState(true);

  function load() {
    setLoading(true);
    getDiscussionPosts(tmdbId, sort).then(setPosts).finally(() => setLoading(false));
  }

  useEffect(() => { load(); }, [tmdbId, sort]);

  async function handleVote(postId, voteType) {
    const result = await voteOnPost(postId, voteType);
    setPosts((prev) => prev.map((p) => (p.postId === postId ? { ...p, score: result.score, myVote: result.myVote } : p)));
  }

  return (
    <div style={{ maxWidth: '700px', margin: '0 auto', padding: '2rem 1.5rem', color: '#fff' }}>
      <h1 style={{ fontSize: '1.6rem', marginBottom: '0.25rem' }}>Discussion</h1>

      <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '1.5rem' }}>
        {['new', 'top'].map((s) => (
          <button
            key={s}
            onClick={() => setSort(s)}
            style={{ background: sort === s ? '#00e054' : '#1c2228', color: sort === s ? '#14181c' : '#9ab', border: '1px solid #2c3440', padding: '0.4rem 1rem', borderRadius: '20px', cursor: 'pointer', fontSize: '0.8rem', fontWeight: 'bold', textTransform: 'capitalize' }}
          >
            {s}
          </button>
        ))}
      </div>

      <NewPostForm tmdbId={tmdbId} onCreated={() => load()} />

      {loading ? (
        <p style={{ color: '#9ab' }}>Loading...</p>
      ) : posts.length === 0 ? (
        <div style={{ background: '#1c2228', border: '1px solid #2c3440', borderRadius: '8px', padding: '2rem', textAlign: 'center', color: '#9ab' }}>
          No discussion yet - be the first to post.
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
          {posts.map((p) => (
            <div key={p.postId} style={{ background: '#1c2228', border: '1px solid #2c3440', borderRadius: '8px', padding: '1.25rem', display: 'flex', gap: '1rem' }}>
              <VoteControls score={p.score} myVote={p.myVote} onVote={(type) => handleVote(p.postId, type)} />
              <div style={{ flex: 1 }}>
                <Link to={`/discussions/post/${p.postId}`} style={{ color: '#fff', fontWeight: 'bold', fontSize: '1.05rem', textDecoration: 'none' }}>
                  {p.title}
                </Link>
                <p style={{ color: '#667', fontSize: '0.8rem', margin: '0.2rem 0 0.5rem' }}>
                  @{p.user.username} &middot; {p.commentCount} comments
                </p>
                <SpoilerText text={p.body} isSpoiler={p.isSpoiler} style={{ color: '#c1c8d0', fontSize: '0.9rem', margin: 0 }} />
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}