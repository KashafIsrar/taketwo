// components/MessageButton.jsx
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getOrCreateDm } from '../services/api';

export default function MessageButton({ targetUserId }) {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);

  async function handleClick() {
    setLoading(true);
    try {
      const conversation = await getOrCreateDm(targetUserId);
      navigate(`/messages/${conversation.conversationId}`);
    } catch (err) {
      console.error('Failed to start conversation', err);
    } finally {
      setLoading(false);
    }
  }

  return (
    <button
      onClick={handleClick}
      disabled={loading}
      style={{ background: 'transparent', border: '1px solid #2c3440', color: '#fff', padding: '6px 16px', borderRadius: '20px', fontWeight: 'bold', cursor: 'pointer' }}
    >
      {loading ? '...' : 'Message'}
    </button>
  );
}