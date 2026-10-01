import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import styles from './Navbar.module.css';

export default function Navbar() {
  const { user, isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();
  const [query, setQuery] = useState('');

  // Check every possible place the user ID could be stored
  const userId = user?.id || user?.userId || user?._id || localStorage.getItem('userId');

  function handleSearch(e) {
    e.preventDefault();
    if (query.trim()) {
      navigate(`/?q=${encodeURIComponent(query.trim())}`);
    }
  }

  function handleLogout() {
    logout();
    navigate('/');
  }

  return (
    <nav className={styles.bar}>
      <Link to="/" className={styles.logo}>TAKE<span>TWO</span></Link>

      <div className={styles.links}>
        <Link to="/">Discover</Link>
        {isAuthenticated && (
          <>
            <Link to="/activity">Activity</Link>
            <Link to="/members">Members</Link>
            <Link to="/diary">Diary</Link>
            <Link to="/lists">Lists</Link>
          </>
        )}
      </div>

      <form className={styles.searchForm} onSubmit={handleSearch}>
        <input
          type="search"
          placeholder="Search movies…"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
        />
      </form>

      <div className={styles.right}>
        {isAuthenticated ? (
          <>
            {userId ? (
              <Link to={`/user/${userId}`} className={styles.username} style={{ textDecoration: 'none', color: '#00e054', fontWeight: 'bold' }}>
                {user.displayName || user.username}
              </Link>
            ) : (
              // Fallback if ID is missing: link to the members page so you can find your profile easily
              <Link to="/members" className={styles.username} style={{ textDecoration: 'none', color: '#00e054' }}>
                {user?.displayName || user?.username || 'Profile'}
              </Link>
            )}
            <button className={styles.logoutBtn} onClick={handleLogout}>Log out</button>
          </>
        ) : (
          <>
            <Link to="/login">Log in</Link>
            <Link to="/register">Register</Link>
          </>
        )}
      </div>
    </nav>
  );
}