import { useEffect, useState } from 'react';
import api from '../services/api';
import styles from './GamificationDashboard.module.css';

export default function GamificationDashboard({ userId = 1 }) {
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [fetchError, setFetchError] = useState(null);

  useEffect(() => {
    if (!userId) {
      setLoading(false);
      return;
    }
    
    setLoading(true);
    api.get(`/gamification/profile/${userId}`)
      .then(res => {
        setProfile(res.data);
        setFetchError(null);
      })
      .catch((err) => {
        console.error("Gamification fetch error:", err);
        setFetchError(err.message || 'Failed to load gamification profile');
        setProfile(null);
      })
      .finally(() => setLoading(false));
  }, [userId]);

  if (loading) return <div className={styles.loading}>Loading achievements...</div>;

  return (
    <div className={styles.container}>
      <h2>Cinephile Achievements & Stats</h2>

      {fetchError && (
        <div style={{ background: '#2c1515', border: '1px solid #ff4e4e', color: '#ff4e4e', padding: '10px', borderRadius: '6px', marginBottom: '15px', fontSize: '0.9rem' }}>
          <strong>Backend Error:</strong> Could not load gamification data from API ({fetchError}). Check if your backend route `/gamification/profile/${userId}` is running!
        </div>
      )}

      {/* Streaks Banner */}
      <div className={styles.streakCard}>
        <div className={styles.streakItem}>
          <div className={styles.streakInfo}>
            <h3>{profile?.currentStreakDays || 0} Days</h3>
            <p>Current Watch Streak</p>
          </div>
        </div>
        <div className={styles.streakItem}>
          <div className={styles.streakInfo}>
            <h3>{profile?.totalLogged || 0}</h3>
            <p>Total Films Logged</p>
          </div>
        </div>
      </div>

      {/* Badges Section */}
      <div className={styles.section}>
        <h3>Badges & Milestones</h3>
        <div className={styles.badgeGrid}>
          {profile?.badges && profile.badges.length > 0 ? (
            profile.badges.map(badge => (
              <div key={badge.id} className={`${styles.badgeItem} ${badge.unlocked ? styles.unlocked : styles.locked}`}>
                <div>
                  <h4>{badge.title}</h4>
                  <p>{badge.description}</p>
                  <span className={styles.status}>{badge.status}</span>
                </div>
              </div>
            ))
          ) : (
            <p style={{ color: '#888', fontSize: '0.9rem' }}>No badges unlocked yet.</p>
          )}
        </div>
      </div>

      {/* Challenges Section */}
      <div className={styles.section}>
        <h3>Custom Watch Challenges</h3>
        {profile?.activeChallenges && profile.activeChallenges.length > 0 ? (
          profile.activeChallenges.map((challenge, idx) => (
            <div key={idx} className={styles.challengeCard}>
              <div className={styles.challengeInfo}>
                <h4>{challenge.title}</h4>
                <p>{challenge.description}</p>
              </div>
              <div className={styles.progressWrapper}>
                <span>{challenge.current} / {challenge.goal}</span>
                <div className={styles.progressBar}>
                  <div 
                    className={styles.progressFill} 
                    style={{ width: `${Math.min(100, (challenge.current / challenge.goal) * 100)}%` }}
                  ></div>
                </div>
              </div>
            </div>
          ))
        ) : (
          <p style={{ color: '#888', fontSize: '0.9rem' }}>No active challenges found.</p>
        )}
      </div>
    </div>
  );
}