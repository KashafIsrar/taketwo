import { useState } from 'react';
import styles from './StarRating.module.css';

const STAR_COUNT = 5;

export default function StarRating({ value = 0, onChange, readOnly = false, size = 26, showValue = false }) {
  const [hoverValue, setHoverValue] = useState(null);
  const displayValue = hoverValue ?? value;

  function handleInteraction(starIndex, event, commit) {
    if (readOnly || !onChange) return;
    const rect = event.currentTarget.getBoundingClientRect();
    const isLeftHalf = event.clientX - rect.left < rect.width / 2;
    const next = starIndex + (isLeftHalf ? 0.5 : 1);
    if (commit) {
      onChange(next);
    } else {
      setHoverValue(next);
    }
  }

  return (
    <span className={styles.row} onMouseLeave={() => setHoverValue(null)}>
      {Array.from({ length: STAR_COUNT }).map((_, i) => {
        const fillPercent = Math.max(0, Math.min(1, displayValue - i)) * 100;
        return (
          <span
            key={i}
            style={{
              position: 'relative',
              display: 'inline-block',
              width: `${size}px`,
              height: `${size}px`,
              fontSize: `${size}px`,
              lineHeight: 1,
              cursor: readOnly ? 'default' : 'pointer',
            }}
            onMouseMove={(e) => handleInteraction(i, e, false)}
            onClick={(e) => handleInteraction(i, e, true)}
            role={readOnly ? undefined : 'button'}
            aria-label={readOnly ? undefined : `Rate ${i + 1} stars`}
          >
            {/* Unfilled star background */}
            <span style={{ position: 'absolute', top: 0, left: 0, color: '#2a3b32', zIndex: 1 }}>★</span>
            {/* Filled star foreground with dynamic clipping */}
            <span
              style={{
                position: 'absolute',
                top: 0,
                left: 0,
                color: '#ffb703',
                overflow: 'hidden',
                width: `${fillPercent}%`,
                whiteSpace: 'nowrap',
                zIndex: 2,
                textShadow: '0 0 10px rgba(255, 183, 3, 0.6)',
              }}
            >
              ★
            </span>
          </span>
        );
      })}
      {showValue && <span className={styles.value}>{value ? value.toFixed(1) : '–'} / 5</span>}
    </span>
  );
}