import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getActivityFeed } from '../services/api';
import LikeCommentSection from '../components/LikeCommentSection';
import styles from './DiscoverPage.module.css';

export default function ActivityPage() {
  const [feed, setFeed] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

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

    loadActivity();
    return () => { cancelled = true; };
  }, []);

  if (loading) {
    return <div className={styles.page}><p className={styles.state}>Loading activity…</p></div>;
  }

  if (error) {
    return <div className={styles.page}><p className={styles.state}>{error}</p></div>;
  }

  return (
    <div className={styles.page}>
      <div className={styles.section} style={{ maxWidth: '700px', margin: '0 auto', padding: '2rem 1rem' }}>
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