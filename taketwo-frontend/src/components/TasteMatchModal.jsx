// components/TasteMatchModal.jsx
import FollowButton from './FollowButton';

export default function TasteMatchModal({ match, onClose, targetUserId }) {
  return (
    <div style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.8)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000, padding: '1rem' }}>
      <div style={{ background: '#1c2228', border: '1px solid #2c3440', borderRadius: '8px', padding: '2rem', width: '100%', maxWidth: '480px', maxHeight: '85vh', overflowY: 'auto' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '1rem' }}>
          <div>
            <h2 style={{ margin: 0, color: '#fff' }}>{match.matchPercent}% Match</h2>
            <p style={{ margin: '0.25rem 0 0', color: '#9ab' }}>with @{match.targetUsername}</p>
          </div>
          <button onClick={onClose} style={{ background: 'none', border: 'none', color: '#9ab', fontSize: '1.3rem', cursor: 'pointer' }}>&times;</button>
        </div>

        {match.ratingAlignmentPercent != null && (
          <p style={{ color: '#c1c8d0', fontSize: '0.9rem' }}>
            You've both watched {match.commonlyWatchedCount} of the same films, and rate them
            {' '}{Math.round(match.ratingAlignmentPercent)}% similarly.
          </p>
        )}

        {match.sharedGenres.length > 0 && (
          <div style={{ marginTop: '1rem' }}>
            <h4 style={{ color: '#9ab', fontSize: '0.8rem', textTransform: 'uppercase', marginBottom: '0.5rem' }}>Shared Genres</h4>
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem' }}>
              {match.sharedGenres.map((g) => (
                <span key={g.genreName} style={{ background: '#14181c', border: '1px solid #2c3440', color: '#fff', padding: '0.3rem 0.8rem', borderRadius: '20px', fontSize: '0.8rem' }}>
                  {g.genreName}
                </span>
              ))}
            </div>
          </div>
        )}

        {match.sharedDirectors.length > 0 && (
          <div style={{ marginTop: '1rem' }}>
            <h4 style={{ color: '#9ab', fontSize: '0.8rem', textTransform: 'uppercase', marginBottom: '0.5rem' }}>Shared Directors</h4>
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem' }}>
              {match.sharedDirectors.map((d) => (
                <span key={d.name} style={{ background: '#14181c', border: '1px solid #2c3440', color: '#fff', padding: '0.3rem 0.8rem', borderRadius: '20px', fontSize: '0.8rem' }}>
                  {d.name}
                </span>
              ))}
            </div>
          </div>
        )}

        {match.moviesTheyLovedYouHaventSeen.length > 0 && (
          <div style={{ marginTop: '1.25rem' }}>
            <h4 style={{ color: '#9ab', fontSize: '0.8rem', textTransform: 'uppercase', marginBottom: '0.5rem' }}>
              @{match.targetUsername} loved these - you haven't logged them
            </h4>
            <div style={{ display: 'flex', gap: '0.6rem', overflowX: 'auto', paddingBottom: '0.3rem' }}>
              {match.moviesTheyLovedYouHaventSeen.map((m) => (
                <img key={m.tmdbId} src={m.posterUrl} alt={m.title} title={m.title}
                     style={{ width: '70px', borderRadius: '4px', flexShrink: 0 }} />
              ))}
            </div>
          </div>
        )}

        <div style={{ display: 'flex', gap: '0.75rem', marginTop: '1.5rem' }}>
          <FollowButton targetUserId={targetUserId} />
          {/* Direct Message / Invite to Group Chat slot in here once messaging exists */}
        </div>
      </div>
    </div>
  );
}