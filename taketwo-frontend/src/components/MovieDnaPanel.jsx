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
  const [status, setStatus] = useState('loading'); // 'loading' | 'ready' | 'error'

  useEffect(() => {
    let active = true;
    setStatus('loading');
    api.get(`/movies/${tmdbId}/dna`)
      .then((res) => {
        if (active) {
          setDna(res.data);
          setStatus('ready');
        }
      })
      .catch(() => {
        if (active) setStatus('error');
      });
    return () => { active = false; };
  }, [tmdbId]);

  const boxStyle = { background: '#1c2228', border: '1px solid #2c3440', borderRadius: '8px', padding: '1.25rem', marginTop: '1.5rem' };

  if (status === 'loading') {
    return (
      <div style={boxStyle}>
        <p style={{ color: '#9ab', fontSize: '0.85rem', margin: 0 }}>Analyzing Movie DNA...</p>
      </div>
    );
  }

  if (status === 'error') {
    return (
      <div style={boxStyle}>
        <p style={{ color: '#9ab', fontSize: '0.85rem', margin: 0 }}>Movie DNA is unavailable right now.</p>
      </div>
    );
  }

  if (!dna || dna.source === 'NONE') {
    return (
      <div style={boxStyle}>
        <p style={{ color: '#9ab', fontSize: '0.85rem', margin: 0 }}>Not enough data to build a Movie DNA profile for this film yet.</p>
      </div>
    );
  }

  return (
    <div style={boxStyle}>
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

      {dna.source === 'GENRE' && (
        <p style={{ color: '#667', fontSize: '0.75rem', margin: '0.75rem 0 0' }}>Estimated from genre</p>
      )}
    </div>
  );
}