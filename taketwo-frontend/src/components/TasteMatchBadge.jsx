// components/TasteMatchBadge.jsx
import { useState, useEffect } from 'react';
import api from '../services/api';
import TasteMatchModal from './TasteMatchModal';

export default function TasteMatchBadge({ targetUserId }) {
  const [match, setMatch] = useState(null);
  const [modalOpen, setModalOpen] = useState(false);

  useEffect(() => {
    let active = true;
    if (!targetUserId) return;
    api.get(`/taste-profile/match/${targetUserId}`)
      .then((res) => { if (active) setMatch(res.data); })
      .catch(() => {});
    return () => { active = false; };
  }, [targetUserId]);

  if (!match) return null;

  return (
    <>
      <button
        onClick={() => setModalOpen(true)}
        style={{ background: '#1c2228', border: '1px solid #00e054', color: '#00e054', padding: '6px 14px', borderRadius: '20px', fontWeight: 'bold', cursor: 'pointer', fontSize: '0.85rem' }}
      >
        {match.matchPercent}% Taste Match
      </button>
      {modalOpen && <TasteMatchModal match={match} onClose={() => setModalOpen(false)} targetUserId={targetUserId} />}
    </>
  );
}