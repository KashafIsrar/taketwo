import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import api, { getUserProfile, searchMovies } from '../services/api';
import FollowButton from '../components/FollowButton';
import GamificationDashboard from '../components/GamificationDashboard.jsx';
import TasteMatchBadge from '../components/TasteMatchBadge';

const AVATAR_CATEGORIES = [
  {
    category: 'Breaking Bad',
    avatars: [
      { id: 'bb_1', name: 'Walter White', url: 'https://i.pinimg.com/1200x/e7/85/00/e7850038e586666e89451a9e0f101bbe.jpg' },
      { id: 'bb_2', name: 'Jesse Pinkman', url: 'https://i.pinimg.com/736x/c9/6e/4b/c96e4be76a8122766217d1283cc12d44.jpg' },
      { id: 'bb_3', name: 'Gustavo Fring', url: 'https://i.pinimg.com/1200x/15/26/2d/15262d4e9b95fd3d64f29a1b3e132c50.jpg' }
    ]
  },
  {
    category: 'Fight Club',
    avatars: [
      { id: 'fc_1', name: 'Tyler Durden', url: 'https://i.pinimg.com/736x/d4/3a/29/d43a2930bf48d679b2c2709bb52b1c3a.jpg' },
      { id: 'fc_2', name: 'The Narrator', url: 'https://i.pinimg.com/736x/af/fb/35/affb353b1df44d289a12d04b8fa6e541.jpg' }
    ]
  },
  {
    category: 'The Office',
    avatars: [
      { id: 'off_1', name: 'Michael Scott', url: 'https://i.pinimg.com/736x/d6/3d/ee/d63dee84adc4695b6aaa46ccc4882b06.jpg' },
      { id: 'off_2', name: 'Dwight Schrute', url: 'https://i.pinimg.com/736x/41/1e/58/411e58a366f23254c61a81ee1f32cf30.jpg' }
    ]
  },
  {
    category: 'BoJack Horseman',
    avatars: [
      { id: 'bj_1', name: 'BoJack', url: 'https://i.pinimg.com/736x/8b/90/2c/8b902c165e83dfdcacc8e00312551707.jpg' },
      { id: 'bj_2', name: 'Diane', url: 'https://i.pinimg.com/736x/e5/82/4d/e5824d32df0bceff4e74e5b8b2583479.jpg' }
    ]
  },
  {
    category: 'Modern Family',
    avatars: [
      { id: 'mf_1', name: 'Phil Dunphy', url: 'https://i.pinimg.com/736x/8a/e0/be/8ae0be8f3cf0f1bdd684013cdd84ae2a.jpg' },
      { id: 'mf_2', name: 'Gloria', url: 'https://i.pinimg.com/736x/0c/18/ee/0c18ee7cf6d790a5b80239b875b85da9.jpg' }
    ]
  },
  {
    category: 'Game of Thrones',
    avatars: [
      { id: 'got_1', name: 'Jon Snow', url: 'https://i.pinimg.com/1200x/1b/ee/de/1beede75606ba190d6b12593121ccadc.jpg' },
      { id: 'got_2', name: 'Daenerys', url: 'https://i.pinimg.com/1200x/38/13/b6/3813b69df92233f44c5d54303b3b0c32.jpg' }
    ]
  },
  {
    category: 'American Psycho',
    avatars: [
      { id: 'ap_1', name: 'Patrick Bateman', url: 'https://i.pinimg.com/736x/9d/75/28/9d7528fde22802fbdd5fe54737c77dae.jpg' }
    ]
  },
  {
    category: 'Shrek',
    avatars: [
      { id: 'shk_1', name: 'Shrek', url: 'https://i.pinimg.com/736x/26/fd/65/26fd656c3a995a0035e75f8278db8e6b.jpg' }
    ]
  },
  {
    category: 'Dark',
    avatars: [
      { id: 'dark_1', name: 'Jonas Kahnwald', url: 'https://i.pinimg.com/736x/21/da/9d/21da9d1a81458d6204bfd8e1ee396d68.jpg' }
    ]
  }
];

export default function ProfilePage() {
  const { userId: paramUserId } = useParams();
  const { user } = useAuth();
  
  const activeUserId = user?.id || user?.userId || localStorage.getItem('userId');
  const userId = paramUserId || activeUserId;

  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [activeTab, setActiveTab] = useState('overview');
  const [followersList, setFollowersList] = useState([]);
  const [followingList, setFollowingList] = useState([]);
  const [listLoading, setListLoading] = useState(false);

  // Edit Profile Modal States
  const [isEditingProfile, setIsEditingProfile] = useState(false);
  const [editDisplayName, setEditDisplayName] = useState('');
  const [editBio, setEditBio] = useState('');
  const [editProfilePictureUrl, setEditProfilePictureUrl] = useState('');
  const [savingProfile, setSavingProfile] = useState(false);

  // Favorite Four Modal States
  const [isSelectingSlot, setIsSelectingSlot] = useState(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState([]);
  const [searching, setSearching] = useState(false);

  useEffect(() => {
    if (!userId) {
      setError('User not found.');
      setLoading(false);
      return;
    }

    let active = true;
    setLoading(true);
    setActiveTab('overview');

    getUserProfile(userId)
      .then((data) => {
        if (active) {
          setProfile(data);
          setEditDisplayName(data.displayName || data.username || '');
          setEditBio(data.bio || '');
          setEditProfilePictureUrl(data.profilePictureUrl || '');
          setLoading(false);
        }
      })
      .catch((err) => {
        if (active) {
          setError('Failed to load user profile');
          setLoading(false);
        }
      });

    return () => { active = false; };
  }, [userId]);

  useEffect(() => {
    if (!userId) return;

    if (activeTab === 'followers') {
      setListLoading(true);
      api.get(`/users/${userId}/followers`)
        .then(res => setFollowersList(Array.isArray(res.data) ? res.data : []))
        .catch(() => setFollowersList([]))
        .finally(() => setListLoading(false));
    } else if (activeTab === 'following') {
      setListLoading(true);
      api.get(`/users/${userId}/following`)
        .then(res => setFollowingList(Array.isArray(res.data) ? res.data : []))
        .catch(() => setFollowingList([]))
        .finally(() => setListLoading(false));
    }
  }, [activeTab, userId]);

  useEffect(() => {
    if (!searchQuery.trim()) {
      setSearchResults([]);
      return;
    }

    const timer = setTimeout(async () => {
      setSearching(true);
      try {
        const data = await searchMovies(searchQuery);
        setSearchResults(Array.isArray(data) ? data : data.results || []);
      } catch (err) {
        setSearchResults([]);
      } finally {
        setSearching(false);
      }
    }, 300);

    return () => clearTimeout(timer);
  }, [searchQuery]);

  const handleSelectMovieForSlot = async (movie) => {
    if (isSelectingSlot === null) return;

    const updatedFavorites = [...(profile.favoriteMovies || [])];
    while(updatedFavorites.length <= isSelectingSlot) {
      updatedFavorites.push(null);
    }

    updatedFavorites[isSelectingSlot] = {
      id: movie.id || movie.tmdbId,
      tmdbId: movie.tmdbId || movie.id,
      title: movie.title,
      posterUrl: movie.posterPath || movie.posterUrl
    };

    setProfile(prev => ({ ...prev, favoriteMovies: updatedFavorites }));
    setIsSelectingSlot(null);
    setSearchQuery('');
    setSearchResults([]);

    try {
      await api.put('/users/favorites', { favoriteMovies: updatedFavorites });
    } catch (err) {
      console.error('Failed to update favorite films', err);
    }
  };

  const handleSaveProfile = async (e) => {
    e.preventDefault();
    setSavingProfile(true);
    try {
      await api.put('/users/profile', {
        displayName: editDisplayName,
        bio: editBio,
        profilePictureUrl: editProfilePictureUrl
      });

      setProfile(prev => ({
        ...prev,
        displayName: editDisplayName,
        bio: editBio,
        profilePictureUrl: editProfilePictureUrl
      }));
      setIsEditingProfile(false);
    } catch (err) {
      console.error('Failed to update profile', err);
      alert('Failed to update profile.');
    } finally {
      setSavingProfile(false);
    }
  };

  if (loading) return <div style={{ color: '#fff', padding: '3rem', textAlign: 'center' }}>Loading profile…</div>;
  if (error) return <div style={{ color: '#ff4e4e', padding: '3rem', textAlign: 'center' }}>{error}</div>;
  if (!profile) return <div style={{ color: '#fff', padding: '3rem', textAlign: 'center' }}>User not found.</div>;

  const isSelf = String(activeUserId) === String(userId);
  const favoriteFour = profile.favoriteMovies || [];

  return (
    <div style={{ padding: '2rem', color: '#fff', maxWidth: '800px', margin: '0 auto', position: 'relative' }}>
      
      {/* Profile Header & Bio Section */}
      <div style={{ display: 'flex', alignItems: 'flex-start', gap: '1.5rem', marginBottom: '2rem' }}>
        <div style={{ width: '90px', height: '90px', borderRadius: '50%', background: '#2c3440', overflow: 'hidden', flexShrink: '0', display: 'flex', alignItems: 'center', justifyContent: 'center', border: '2px solid #3c4450', boxShadow: '0 4px 12px rgba(0,0,0,0.3)' }}>
          {profile.profilePictureUrl ? (
            <img src={profile.profilePictureUrl} alt="Avatar" style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
          ) : (
            <span style={{ fontSize: '2rem', fontWeight: 'bold', color: '#9ab' }}>
              {(profile.displayName || profile.username || '?').charAt(0).toUpperCase()}
            </span>
          )}
        </div>

        <div style={{ flexGrow: 1 }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: '1rem' }}>
            <div>
              <h1 style={{ margin: 0, fontSize: '1.8rem' }}>{profile.displayName || profile.username}</h1>
              <p style={{ color: '#888', margin: '0.2rem 0 0.5rem 0' }}>@{profile.username}</p>
            </div>
            
            <div style={{ display: 'flex', gap: '0.75rem', alignItems: 'center' }}>
              {isSelf && (
                <button 
                  onClick={() => setIsEditingProfile(true)}
                  style={{ background: '#2c3440', color: '#fff', border: '1px solid #4c5460', padding: '0.5rem 1rem', borderRadius: '4px', cursor: 'pointer', fontWeight: '600', fontSize: '0.9rem', transition: 'background 0.2s' }}
                  onMouseEnter={(e) => e.currentTarget.style.background = '#3c4450'}
                  onMouseLeave={(e) => e.currentTarget.style.background = '#2c3440'}
                >
                  Edit Profile
                </button>
              )}
              {!isSelf && (
                <>
                  <TasteMatchBadge targetUserId={userId} />
                  <FollowButton
                    targetUserId={userId}
                    onStatusChange={(status) => {
                      setProfile((prev) => ({
                        ...prev,
                        followerCount: status.following ? prev.followerCount + 1 : prev.followerCount - 1,
                      }));
                    }}
                  />
                </>
              )}
            </div>
          </div>

          <p style={{ color: '#ccc', margin: '0.5rem 0 0 0', fontSize: '0.95rem', lineHeight: '1.4', whiteSpace: 'pre-wrap' }}>
            {profile.bio || (isSelf ? 'No bio added yet. Click "Edit Profile" to write one!' : '')}
          </p>
        </div>
      </div>

      {/* Favorite Four Section */}
      <div style={{ marginBottom: '2rem' }}>
        <h3 style={{ fontSize: '0.9rem', color: '#9ab', textTransform: 'uppercase', letterSpacing: '1px', marginBottom: '0.75rem', fontWeight: '600' }}>Favorite Films</h3>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '1rem' }}>
          {[0, 1, 2, 3].map((index) => {
            const movie = favoriteFour[index];
            return (
              <div 
                key={index} 
                onClick={() => isSelf && setIsSelectingSlot(index)}
                style={{ 
                  aspectRatio: '2/3', 
                  background: '#1c2228', 
                  borderRadius: '6px', 
                  border: '1px solid #2c3440', 
                  display: 'flex', 
                  alignItems: 'center', 
                  justifyContent: 'center', 
                  overflow: 'hidden',
                  position: 'relative',
                  cursor: isSelf ? 'pointer' : 'default',
                  transition: 'all 0.2s ease',
                }}
                onMouseEnter={(e) => {
                  if (isSelf) {
                    e.currentTarget.style.transform = 'translateY(-3px)';
                    e.currentTarget.style.borderColor = '#00e054';
                    e.currentTarget.style.boxShadow = '0 6px 16px rgba(0,224,84,0.15)';
                  }
                }}
                onMouseLeave={(e) => {
                  if (isSelf) {
                    e.currentTarget.style.transform = 'translateY(0)';
                    e.currentTarget.style.borderColor = '#2c3440';
                    e.currentTarget.style.boxShadow = 'none';
                  }
                }}
                title={isSelf ? "Click to set favorite film" : ""}
              >
                {movie && (movie.posterUrl || movie.posterPath) ? (
                  <img 
                    src={movie.posterUrl?.startsWith('http') ? movie.posterUrl : `https://image.tmdb.org/t/p/w500${movie.posterUrl || movie.posterPath}`} 
                    alt={movie.title} 
                    style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                  />
                ) : (
                  <span style={{ color: '#455', fontSize: '1rem', fontWeight: 'bold' }}>+{index + 1}</span>
                )}
              </div>
            );
          })}
        </div>
      </div>

      {/* Stats Bar */}
      <div style={{ display: 'flex', gap: '2rem', marginBottom: '1.5rem', background: '#1c2228', padding: '1.5rem', borderRadius: '8px', border: '1px solid #2c3440', boxShadow: '0 4px 12px rgba(0,0,0,0.2)' }}>
        <div>
          <h3 style={{ margin: 0, color: '#00e054', fontSize: '1.5rem' }}>{profile.totalLoggedFilms || profile.totalLogged || 0}</h3>
          <p style={{ margin: '0.25rem 0 0 0', color: '#9ab', fontSize: '0.85rem' }}>Films Logged</p>
        </div>
        <div>
          <h3 style={{ margin: 0, color: '#00e054', fontSize: '1.5rem' }}>{profile.averageRating ? profile.averageRating.toFixed(1) : 'N/A'}</h3>
          <p style={{ margin: '0.25rem 0 0 0', color: '#9ab', fontSize: '0.85rem' }}>Avg Rating</p>
        </div>
        <div style={{ cursor: 'pointer' }} onClick={() => setActiveTab('followers')}>
          <h3 style={{ margin: 0, color: '#00e054', fontSize: '1.5rem' }}>{profile.followerCount || 0}</h3>
          <p style={{ margin: '0.25rem 0 0 0', color: '#9ab', fontSize: '0.85rem', textDecoration: 'underline' }}>Followers</p>
        </div>
        <div style={{ cursor: 'pointer' }} onClick={() => setActiveTab('following')}>
          <h3 style={{ margin: 0, color: '#00e054', fontSize: '1.5rem' }}>{profile.followingCount || 0}</h3>
          <p style={{ margin: '0.25rem 0 0 0', color: '#9ab', fontSize: '0.85rem', textDecoration: 'underline' }}>Following</p>
        </div>
      </div>

      {/* Gamification Dashboard (Always Visible) */}
      <div style={{ marginBottom: '2rem' }}>
        <GamificationDashboard userId={userId} />
      </div>

      {/* Profile Navigation Tabs */}
      <div style={{ display: 'flex', gap: '1.5rem', borderBottom: '1px solid #2c3440', marginBottom: '1.5rem', paddingBottom: '0.5rem' }}>
        <button onClick={() => setActiveTab('overview')} style={{ background: 'none', border: 'none', color: activeTab === 'overview' ? '#00e054' : '#9ab', cursor: 'pointer', fontWeight: 'bold', fontSize: '1rem', padding: 0 }}>Overview</button>
        <button onClick={() => setActiveTab('followers')} style={{ background: 'none', border: 'none', color: activeTab === 'followers' ? '#00e054' : '#9ab', cursor: 'pointer', fontWeight: 'bold', fontSize: '1rem', padding: 0 }}>Followers ({profile.followerCount || 0})</button>
        <button onClick={() => setActiveTab('following')} style={{ background: 'none', border: 'none', color: activeTab === 'following' ? '#00e054' : '#888', cursor: 'pointer', fontWeight: 'bold', fontSize: '1rem', padding: 0 }}>Following ({profile.followingCount || 0})</button>
      </div>

      {activeTab === 'overview' && (
        <div style={{ color: '#9ab' }}>
          <p>Welcome to {profile.username}'s profile overview.</p>
        </div>
      )}
      
      {activeTab === 'followers' && (
        <div>
          {listLoading ? <p style={{ color: '#888' }}>Loading…</p> : followersList.length === 0 ? <p style={{ color: '#888' }}>No followers yet.</p> : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
              {followersList.map(f => (
                <div key={f.id || f.userId} style={{ background: '#1c2228', padding: '0.75rem 1rem', borderRadius: '6px', border: '1px solid #2c3440' }}>
                  <Link to={`/user/${f.id || f.userId}`} style={{ color: '#fff', textDecoration: 'none', fontWeight: '500' }}>@{f.username}</Link>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {activeTab === 'following' && (
        <div>
          {listLoading ? <p style={{ color: '#888' }}>Loading…</p> : followingList.length === 0 ? <p style={{ color: '#888' }}>Not following anyone yet.</p> : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
              {followingList.map(f => (
                <div key={f.id || f.userId} style={{ background: '#1c2228', padding: '0.75rem 1rem', borderRadius: '6px', border: '1px solid #2c3440' }}>
                  <Link to={`/user/${f.id || f.userId}`} style={{ color: '#fff', textDecoration: 'none', fontWeight: '500' }}>@{f.username}</Link>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* Edit Profile Modal */}
      {isEditingProfile && (
        <div style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.8)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000, padding: '1rem', backdropFilter: 'blur(4px)' }}>
          <div style={{ background: '#1c2228', border: '1px solid #2c3440', padding: '1.5rem', borderRadius: '8px', width: '100%', maxWidth: '520px', display: 'flex', flexDirection: 'column', gap: '1rem', maxHeight: '90vh', overflowY: 'auto', boxShadow: '0 8px 32px rgba(0,0,0,0.6)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <h3 style={{ margin: 0, fontSize: '1.1rem', color: '#fff' }}>Edit Profile</h3>
              <button onClick={() => setIsEditingProfile(false)} style={{ background: 'none', border: 'none', color: '#9ab', cursor: 'pointer', fontSize: '1.25rem' }}>&times;</button>
            </div>

            <form onSubmit={handleSaveProfile} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <div>
                <label style={{ display: 'block', color: '#9ab', fontSize: '0.85rem', marginBottom: '0.3rem' }}>Display Name</label>
                <input 
                  type="text" 
                  value={editDisplayName} 
                  onChange={(e) => setEditDisplayName(e.target.value)} 
                  style={{ width: '100%', padding: '0.75rem', background: '#14181c', border: '1px solid #2c3440', borderRadius: '4px', color: '#fff', outline: 'none', fontSize: '1rem' }}
                />
              </div>

              <div>
                <label style={{ display: 'block', color: '#9ab', fontSize: '0.85rem', marginBottom: '0.3rem' }}>Bio</label>
                <textarea 
                  value={editBio} 
                  onChange={(e) => setEditBio(e.target.value)} 
                  maxLength={255}
                  rows={3}
                  placeholder="Tell us about yourself..."
                  style={{ width: '100%', padding: '0.75rem', background: '#14181c', border: '1px solid #2c3440', borderRadius: '4px', color: '#fff', outline: 'none', fontSize: '1rem', resize: 'vertical' }}
                />
              </div>

              <div>
                <label style={{ display: 'block', color: '#9ab', fontSize: '0.85rem', marginBottom: '0.5rem' }}>Choose Character Avatar</label>
                
                <div style={{ maxHeight: '260px', overflowY: 'auto', display: 'flex', flexDirection: 'column', gap: '1.25rem', paddingRight: '0.5rem' }}>
                  {AVATAR_CATEGORIES.map((group) => (
                    <div key={group.category}>
                      <h4 style={{ color: '#fff', fontSize: '0.85rem', marginBottom: '0.5rem', fontWeight: '600' }}>{group.category}</h4>
                      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(5, 1fr)', gap: '0.6rem' }}>
                        {group.avatars.map((avatar) => {
                          const isSelected = editProfilePictureUrl === avatar.url;
                          return (
                            <div 
                              key={avatar.id}
                              onClick={() => setEditProfilePictureUrl(avatar.url)}
                              style={{
                                aspectRatio: '1/1',
                                borderRadius: '6px',
                                overflow: 'hidden',
                                cursor: 'pointer',
                                border: isSelected ? '3px solid #00e054' : '2px solid transparent',
                                opacity: isSelected ? 1 : 0.7,
                                transition: 'all 0.2s'
                              }}
                              title={avatar.name}
                            >
                              <img src={avatar.url} alt={avatar.name} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                            </div>
                          );
                        })}
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '0.5rem' }}>
                <button 
                  type="button" 
                  onClick={() => setIsEditingProfile(false)}
                  style={{ background: 'none', border: '1px solid #2c3440', color: '#9ab', padding: '0.5rem 1rem', borderRadius: '4px', cursor: 'pointer' }}
                >
                  Cancel
                </button>
                <button 
                  type="submit" 
                  disabled={savingProfile}
                  style={{ background: '#00e054', border: 'none', color: '#14181c', padding: '0.5rem 1.25rem', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold' }}
                >
                  {savingProfile ? 'Saving...' : 'Save Changes'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Movie Search Modal for Favorite Four */}
      {isSelectingSlot !== null && (
        <div style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.8)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000, padding: '1rem', backdropFilter: 'blur(4px)' }}>
          <div style={{ background: '#1c2228', border: '1px solid #2c3440', padding: '1.5rem', borderRadius: '8px', width: '100%', maxWidth: '450px', display: 'flex', flexDirection: 'column', gap: '1rem', boxShadow: '0 8px 32px rgba(0,0,0,0.6)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <h3 style={{ margin: 0, fontSize: '1.1rem', color: '#fff' }}>Select Favorite Film #{isSelectingSlot + 1}</h3>
              <button onClick={() => { setIsSelectingSlot(null); setSearchQuery(''); setSearchResults([]); }} style={{ background: 'none', border: 'none', color: '#9ab', cursor: 'pointer', fontSize: '1.25rem' }}>&times;</button>
            </div>
            <input 
              type="text" 
              placeholder="Search movie title (e.g. Interstellar)..." 
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              autoFocus
              style={{ width: '100%', padding: '0.75rem', background: '#14181c', border: '1px solid #2c3440', borderRadius: '4px', color: '#fff', outline: 'none', fontSize: '1rem' }}
            />
            
            <div style={{ maxHeight: '280px', overflowY: 'auto', display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
              {searching && <p style={{ color: '#888', textAlign: 'center', margin: '1rem 0' }}>Searching movies...</p>}
              
              {!searching && searchQuery.trim() && searchResults.length === 0 && (
                <p style={{ color: '#888', textAlign: 'center', margin: '1rem 0' }}>No movies found.</p>
              )}

              {searchResults.map(movie => (
                <div 
                  key={movie.id || movie.tmdbId} 
                  onClick={() => handleSelectMovieForSlot(movie)}
                  style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', padding: '0.5rem 0.75rem', background: '#14181c', borderRadius: '4px', cursor: 'pointer', border: '1px solid #2c3440', transition: 'background 0.2s' }}
                  onMouseEnter={(e) => e.currentTarget.style.background = '#22282e'}
                  onMouseLeave={(e) => e.currentTarget.style.background = '#14181c'}
                >
                  <div style={{ width: '36px', height: '52px', background: '#2c3440', borderRadius: '4px', overflow: 'hidden', flexShrink: 0, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                    {(movie.posterPath || movie.posterUrl) ? (
                      <img 
                        src={(movie.posterUrl || movie.posterPath)?.startsWith('http') ? (movie.posterUrl || movie.posterPath) : `https://image.tmdb.org/t/p/w200${movie.posterUrl || movie.posterPath}`} 
                        alt={movie.title} 
                        style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                      />
                    ) : (
                      <span style={{ fontSize: '0.6rem', color: '#666' }}>No Image</span>
                    )}
                  </div>

                  <div style={{ display: 'flex', flexDirection: 'column', flexGrow: 1, overflow: 'hidden' }}>
                    <span style={{ color: '#fff', fontWeight: '500', fontSize: '0.95rem', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>{movie.title}</span>
                    <span style={{ color: '#888', fontSize: '0.8rem' }}>{movie.release_date ? movie.release_date.substring(0, 4) : ''}</span>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}