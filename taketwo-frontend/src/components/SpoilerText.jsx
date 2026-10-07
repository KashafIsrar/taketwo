import { useState } from 'react';

export default function SpoilerText({ text, isSpoiler, style }) {
  const [revealed, setRevealed] = useState(!isSpoiler);

  if (!isSpoiler) {
    return <p style={style}>{text}</p>;
  }

  return (
    <div>
      <span
        onClick={() => setRevealed((r) => !r)}
        style={{ color: '#ff4e4e', fontSize: '0.8rem', cursor: 'pointer', display: 'inline-block', marginBottom: '0.3rem' }}
      >
        {revealed ? 'Hide spoiler' : 'Spoiler - click to reveal'}
      </span>
      <p style={{ ...style, filter: revealed ? 'none' : 'blur(6px)', userSelect: revealed ? 'auto' : 'none' }}>
        {text}
      </p>
    </div>
  );
}