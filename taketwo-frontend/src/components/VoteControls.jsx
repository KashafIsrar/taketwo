export default function VoteControls({ score, myVote, onVote, size = 'normal' }) {
  const fontSize = size === 'small' ? '0.8rem' : '0.95rem';
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
      <button
        onClick={() => onVote('UP')}
        style={{ background: 'none', border: 'none', cursor: 'pointer', color: myVote === 'UP' ? '#00e054' : '#9ab', fontSize, fontWeight: 'bold' }}
      >
        &uarr;
      </button>
      <span style={{ color: '#fff', fontSize, minWidth: '1.2rem', textAlign: 'center' }}>{score}</span>
      <button
        onClick={() => onVote('DOWN')}
        style={{ background: 'none', border: 'none', cursor: 'pointer', color: myVote === 'DOWN' ? '#ff4e4e' : '#9ab', fontSize, fontWeight: 'bold' }}
      >
        &darr;
      </button>
    </div>
  );
}