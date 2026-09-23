import { useState, useEffect } from 'react';
import { getFollowStatus, toggleFollow } from '../services/api';

export default function FollowButton({ targetUserId, onStatusChange }) {
  const [isFollowing, setIsFollowing] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let active = true;
    if (!targetUserId) return;

    getFollowStatus(targetUserId)
      .then((data) => {
        if (active) {
          setIsFollowing(data.following);
          setLoading(false);
        }
      })
      .catch(() => {
        if (active) setLoading(false);
      });

    return () => {
      active = false;
    };
  }, [targetUserId]);

  const handleToggle = async () => {
    setLoading(true);
    try {
      const data = await toggleFollow(targetUserId);
      setIsFollowing(data.following);
      if (onStatusChange) onStatusChange(data);
    } catch (err) {
      console.error('Failed to toggle follow status', err);
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <button disabled>...</button>;

  return (
    <button
      onClick={handleToggle}
      style={{
        padding: '6px 16px',
        borderRadius: '20px',
        border: 'none',
        fontWeight: 'bold',
        cursor: 'pointer',
        backgroundColor: isFollowing ? '#333' : '#00e054',
        color: isFollowing ? '#fff' : '#14181c',
      }}
    >
      {isFollowing ? 'Following' : 'Follow'}
    </button>
  );
}