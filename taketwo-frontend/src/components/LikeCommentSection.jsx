import { useState, useEffect } from 'react';
import api from '../services/api'; // Adjust path to your api helper if needed

export default function LikeCommentSection({ logId }) {
  const [likesCount, setLikesCount] = useState(0);
  const [hasLiked, setHasLiked] = useState(false);
  const [loading, setLoading] = useState(false);

  // Fetch actual like status and count from backend when component mounts
  useEffect(() => {
    let cancelled = false;
    async function fetchLikeData() {
      try {
        const res = await api.get(`/api/movies/logs/${logId}/likes`);
        if (!cancelled) {
          setLikesCount(res.data.count || 0);
          setHasLiked(res.data.hasLiked || false);
        }
      } catch (err) {
        // Fallback silently if endpoint is still being wired
        console.error("Could not fetch like status", err);
      }
    }
    fetchLikeData();
    return () => { cancelled = true; };
  }, [logId]);

  const handleLikeToggle = async () => {
    if (loading) return;
    setLoading(true);

    // Optimistic UI update
    const previousHasLiked = hasLiked;
    const previousCount = likesCount;

    setHasLiked(!previousHasLiked);
    setLikesCount(prev => previousHasLiked ? Math.max(0, prev - 1) : prev + 1);

    try {
      if (previousHasLiked) {
        await api.delete(`/api/movies/logs/${logId}/like`);
      } else {
        await api.post(`/api/movies/logs/${logId}/like`);
      }
    } catch (err) {
      // Revert on error
      setHasLiked(previousHasLiked);
      setLikesCount(previousCount);
      console.error("Failed to toggle like", err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginTop: '0.5rem', paddingTop: '0.5rem', borderTop: '1px solid #2c3440', fontSize: '0.85rem', color: '#9ab' }}>
      <button 
        onClick={handleLikeToggle}
        disabled={loading}
        style={{ 
          background: 'transparent', 
          border: 'none', 
          color: hasLiked ? '#00e054' : '#9ab', 
          cursor: 'pointer', 
          display: 'flex', 
          alignItems: 'center', 
          gap: '0.3rem', 
          fontWeight: 'bold',
          transition: 'color 0.2s'
        }}
      >
        <span style={{ fontSize: '1.1rem' }}>{hasLiked ? '♥' : '♡'}</span> 
        {likesCount} {likesCount === 1 ? 'Like' : 'Likes'}
      </button>
    </div>
  );
}