import { motion } from 'framer-motion';
import { Link } from 'react-router-dom';
import styles from './MovieCard.module.css';

export default function MovieCard({ movie }) {
  const year = movie.releaseDate ? new Date(movie.releaseDate).getFullYear() : null;
  const rating = typeof movie.voteAverage === 'number' ? movie.voteAverage.toFixed(1) : null;

  return (
    <motion.div
      whileHover={{ y: -4, scale: 1.02 }}
      transition={{ duration: 0.2, ease: 'easeOut' }}
      style={{ display: 'block', width: '100%' }}
    >
      <Link to={`/movie/${movie.tmdbId}`} className={styles.card}>
        <div className={styles.posterFrame}>
          <div className={styles.sprockets} />
          <div className={styles.posterWrap}>
            {movie.posterUrl ? (
              <img className={styles.poster} src={movie.posterUrl} alt={movie.title} loading="lazy" />
            ) : (
              <div className={styles.posterPlaceholder}>{movie.title}</div>
            )}
            {rating && <span className={styles.ratingBadge}>★ {rating}</span>}
          </div>
          <div className={styles.sprockets} />
        </div>
        <div className={styles.title}>{movie.title}</div>
        {year && <div className={styles.year}>{year}</div>}
      </Link>
    </motion.div>
  );
}