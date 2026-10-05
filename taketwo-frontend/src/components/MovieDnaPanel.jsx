import { useEffect, useState } from 'react';
import api from '../services/api';

function AxisBar({ label, result }) {
  const percent = ((result.score + 1) / 2) * 100; // -1..1 to 0..100
  return (
    <div style={{ marginBottom: '0.9rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.8rem', color: '#9ab', marginBottom: '0.3rem' }}>
        <span>{label}</span>
        <span>{result.label}</span>
      </div>
      <div style={{ background: '#14181c', borderRadius: '4px', height: '6px', position: 'relative' }}>
        <div style={{ position: 'absolute', left: `${percent}%`, top: '-3px', width: '12px', height: '12px', background: '#00e054', borderRadius: '50%', transform: 'translateX(-50%)' }} />
      </div>
    </div>
  );
}

export default function MovieDnaPanel({ tmdbId }) {
  const [dna, setDna] = useState(null);

  useEffect(() => {
    let active = true;
    api.get(`/movies/${tmdbId}/dna`)
      .then((res) => { if (active) setDna(res.data); })
      .catch(() => {});
    return () => { active = false; };
  }, [tmdbId]);

  if (!dna || !dna.vibeTags || dna.vibeTags.length === 0) return null;

  return (
    <div style={{ background: '#1c2228', border: '1px solid #2c3440', borderRadius: '8px', padding: '1.25rem', marginTop: '1.5rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
        <h3 style={{ margin: 0, color: '#fff', fontSize: '1rem' }}>Movie DNA</h3>
        {dna.matchPercent != null && (
          <span style={{ background: '#14181c', border: '1px solid #00e054', color: '#00e054', padding: '0.2rem 0.7rem', borderRadius: '20px', fontSize: '0.8rem', fontWeight: 'bold' }}>
            {dna.matchPercent}% matches your taste
          </span>
        )}
      </div>

      <AxisBar label="Pacing" result={dna.pacing} />
      <AxisBar label="Tone" result={dna.tone} />
      <AxisBar label="Complexity" result={dna.complexity} />

      <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem', marginTop: '0.75rem' }}>
        {dna.vibeTags.map((tag) => (
          <span key={tag} style={{ background: '#14181c', border: '1px solid #2c3440', color: '#c1c8d0', padding: '0.3rem 0.8rem', borderRadius: '20px', fontSize: '0.8rem' }}>
            {tag}
          </span>
        ))}
      </div>
    </div>
  );
}