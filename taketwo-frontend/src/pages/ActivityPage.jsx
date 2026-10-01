import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api, { getActivityFeed } from '../services/api';
import LikeCommentSection from '../components/LikeCommentSection';
import styles from './DiscoverPage.module.css';

export default function ActivityPage() {
  const [feed, setFeed] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // Notification states
  const [notifications, setNotifications] = useState([]);
  const [notifLoading, setNotifLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError('');

    async function loadActivity() {
      try {
        const feedRes = await getActivityFeed();
        if (!cancelled) {
          setFeed(feedRes);
        }
      } catch (err) {
        if (!cancelled) {
          setError(err.response?.data?.message || 'Could not load activity feed right now.');
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    }

    async function loadNotifications() {
      try {
        const res = await api.get('/notifications');
        if (!cancelled) {
          setNotifications(res.data);
        }
      } catch (err) {
        // fail silently if notifications aren't critical
      } finally {
        if (!cancelled) {
          setNotifLoading(false);
        }
      }
    }

    loadActivity();
    loadNotifications();

    return () => { cancelled = true; };
  }, []);

  async function handleMarkAllRead() {
    try {
      await api.post('/notifications/mark-read');
      setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
    } catch (err) {
      console.error('Failed to mark notifications read', err);
    }
  }

  if (loading) {
    return <div className={styles.page}><p className={styles.state}>Loading activity…</p></div>;
  }

  if (error) {
    return <div className={styles.page}><p className={styles.state}>{error}</p></div>;
  }

  return (
    <div className={styles.page}>
      <div className={styles.section} style={{ maxWidth: '700px', margin: '0 auto', padding: '2rem 1rem' }}>
        
        {/* Notifications Panel Box */}
        {!notifLoading && notifications.length > 0 && (
          <div style={{ background: '#1c2228', border: '1px solid #2c3440', borderRadius: '8px', padding: '1rem', marginBottom: '1.5rem' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.75rem' }}>
              <h3 style={{ margin: 0, fontSize: '1rem', color: '#fff' }}>Notifications</h3>
              <button onClick={handleMarkAllRead} style={{ background: 'none', border: 'none', color: '#00e054', cursor: 'pointer', fontSize: '0.8rem' }}>
                Mark all read
              </button>
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
              {notifications.map((n) => (
                <div key={n.id} style={{ fontSize: '0.85rem', color: n.read ? '#9ab' : '#fff', padding: '0.4rem 0', borderLeft: n.read ? 'none' : '3px solid #00e054', paddingLeft: n.read ? 0 : '0.5rem' }}>
                  <Link to={`/user/${n.actor.userId}`} style={{ color: '#00e054', fontWeight: 'bold', textDecoration: 'none' }}>
                    @{n.actor.username}
                  </Link>{' '}
                  {n.type === 'LIKE' ? 'liked' : 'commented on'} your review of{' '}
                  <strong>{n.movie.title}</strong>
                  {n.commentPreview && <span style={{ color: '#667' }}> — "{n.commentPreview}"</span>}
                </div>
              ))}
            </div>
          </div>
        )}

        <h2 className={styles.sectionTitle} style={{ fontSize: '1.5rem', marginBottom: '1.5rem', borderBottom: '1px solid #2c3440', paddingBottom: '0.5rem', color: '#fff' }}>Friend Activity</h2>
        {!feed || feed.length === 0 ? (
          <div style={{ background: '#1c2228', padding: '2rem', borderRadius: '8px', textAlign: 'center', color: '#9ab', border: '1px solid #2c3440' }}>
            <p>No friend activity yet. Follow other members to see their logs here!</p>
          </div>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            {feed.map((log) => (
              <div key={log.logId} style={{ background: '#1c2228', padding: '1.25rem', borderRadius: '8px', border: '1px solid #2c3440', display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: '0.9rem', color: '#9ab' }}>
                  <span>
                    <Link to={`/user/${log.user?.userId}`} style={{ color: '#00e054', textDecoration: 'none', fontWeight: 'bold' }}>
                      @{log.user?.username || 'Someone'}
                    </Link>
                    {' watched'}
                    {log.rewatch && <span style={{ color: '#667', fontStyle: 'italic' }}> (rewatch)</span>}
                  </span>
                  <span>{log.watchedDate ? new Date(log.watchedDate).toLocaleDateString() : ''}</span>
                </div>

                <div style={{ fontSize: '1.15rem', fontWeight: 'bold', color: '#fff' }}>
                  {log.movie?.title}
                  {log.rating && <span style={{ color: '#00e054', marginLeft: '0.5rem', fontSize: '1rem' }}>★ {log.rating}</span>}
                </div>

                {log.reviewText && (
                  <p style={{ margin: '0.25rem 0 0 0', color: '#c1c8d0', fontStyle: 'italic', fontSize: '0.95rem', background: '#14181c', padding: '0.75rem', borderRadius: '4px', borderLeft: '3px solid #00e054' }}>
                    "{log.reviewText}"
                  </p>
                )}

                <LikeCommentSection logId={log.logId} />
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}