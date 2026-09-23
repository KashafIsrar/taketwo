import { useState } from 'react';
import StarRating from './StarRating.jsx';
import * as movieService from '../services/movieService';
import styles from './LogMovieModal.module.css';

const today = () => new Date().toISOString().slice(0, 10);

export default function LogMovieModal({ movie, isOpen, onClose, onLogged }) {
  const [watchedDate, setWatchedDate] = useState(today());
  const [rating, setRating] = useState(0);
  const [rewatch, setRewatch] = useState(false);
  const [reviewText, setReviewText] = useState('');
  const [containsSpoilers, setContainsSpoilers] = useState(false);
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  if (!isOpen) return null;

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');

    if (!rating) {
      setError('Give it a rating before logging.');
      return;
    }

    setSubmitting(true);
    try {
      const log = await movieService.logMovie({
        tmdbId: movie.tmdbId,
        watchedDate,
        rating,
        rewatch,
        reviewText,
        containsSpoilers,
      });
      onLogged?.(log);
      onClose();
    } catch (err) {
      setError(err.response?.data?.message || 'Could not log this film. Try again.');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className={styles.overlay} onClick={onClose}>
      <div className={styles.modal} onClick={(e) => e.stopPropagation()}>
        <div className={styles.header}>
          <div>
            <h2>Log film</h2>
            <p className={styles.subtitle}>{movie.title}</p>
          </div>
          <button className={styles.closeBtn} onClick={onClose} aria-label="Close">×</button>
        </div>

        <form onSubmit={handleSubmit}>
          <div className={styles.field}>
            <label>Rating</label>
            <div className={styles.ratingWrapper}>
              <StarRating value={rating} onChange={setRating} size={28} showValue />
              {rating > 0 && (
                <button 
                  type="button" 
                  className={styles.clearRatingBtn} 
                  onClick={() => setRating(0)}
                  title="Clear rating"
                >
                  Clear
                </button>
              )}
            </div>
          </div>

          <div className={styles.field}>
            <label htmlFor="watchedDate">Date watched</label>
            <input
              id="watchedDate"
              type="date"
              value={watchedDate}
              max={today()}
              onChange={(e) => setWatchedDate(e.target.value)}
            />
          </div>

          <div className={styles.checkRow}>
            <input
              id="rewatch"
              type="checkbox"
              checked={rewatch}
              onChange={(e) => setRewatch(e.target.checked)}
            />
            <label htmlFor="rewatch">This is a rewatch</label>
          </div>

          <div className={styles.field}>
            <label htmlFor="reviewText">Review (optional)</label>
            <textarea
              id="reviewText"
              placeholder="What did you think?"
              value={reviewText}
              onChange={(e) => setReviewText(e.target.value)}
            />
          </div>

          {reviewText && (
            <div className={styles.checkRow}>
              <input
                id="spoilers"
                type="checkbox"
                checked={containsSpoilers}
                onChange={(e) => setContainsSpoilers(e.target.checked)}
              />
              <label htmlFor="spoilers">Contains spoilers</label>
            </div>
          )}

          {error && <p className={styles.error}>{error}</p>}

          <div className={styles.actions}>
            <button type="button" className={styles.btnSecondary} onClick={onClose}>Cancel</button>
            <button type="submit" className={styles.btnPrimary} disabled={submitting}>
              {submitting ? 'Logging…' : 'Log film'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}