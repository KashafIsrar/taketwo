import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { getUserProfile } from '../services/api';
import FollowButton from '../components/FollowButton';

export default function ProfilePage() {
  const { userId } = useParams();
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let active = true;
    setLoading(true);

    getUserProfile(userId)
      .then((data) => {
        if (active) {
          setProfile(data);
          setLoading(false);
        }
      })
      .catch((err) => {
        if (active) {
          setError('Failed to load user profile');
          setLoading(false);
        }
      });

    return () => {
      active = false;
    };
  }, [userId]);

  if (loading) return <div>Loading profile…</div>;
  if (error) return <div>{error}</div>;
  if (!profile) return <div>User not found.</div>;

  return (
    <div style={{ padding: '2rem', color: '#fff' }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginBottom: '2rem' }}>
        <h1>{profile.username || profile.displayName || 'User Profile'}</h1>
        <FollowButton
          targetUserId={userId}
          onStatusChange={(status) => {
            setProfile((prev) => ({
              ...prev,
              followerCount: status.following ? prev.followerCount + 1 : prev.followerCount - 1,
            }));
          }}
        />
      </div>

      <div style={{ display: 'flex', gap: '2rem', marginBottom: '2rem' }}>
        <div>
          <h3>{profile.totalLoggedFilms || 0}</h3>
          <p>Films Logged</p>
        </div>
        <div>
          <h3>{profile.averageRating ? profile.averageRating.toFixed(1) : 'N/A'}</h3>
          <p>Avg Rating</p>
        </div>
        <div>
          <h3>{profile.followerCount || 0}</h3>
          <p>Followers</p>
        </div>
        <div>
          <h3>{profile.followingCount || 0}</h3>
          <p>Following</p>
        </div>
      </div>
    </div>
  );
}