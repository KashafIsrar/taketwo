import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import AuthLayout from '../components/AuthLayout.jsx';
import styles from '../components/AuthLayout.module.css';

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [emailOrUsername, setEmailOrUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    setSubmitting(true);
    try {
      await login({ emailOrUsername, password });
      navigate('/');
    } catch (err) {
      setError(err.response?.data?.message || 'Login failed');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <AuthLayout
      headline="Find your next obsession."
      subtext="Log what you watch, rate it your way, and let TAKETWO learn the taste only you have."
    >
      <h1>Welcome back</h1>
      <p className={styles.formSubtitle}>Log in to pick up where you left off.</p>

      <form onSubmit={handleSubmit}>
        <div className={styles.field}>
          <label htmlFor="emailOrUsername">Email or username</label>
          <input
            id="emailOrUsername"
            value={emailOrUsername}
            onChange={(e) => setEmailOrUsername(e.target.value)}
          />
        </div>
        <div className={styles.field}>
          <label htmlFor="password">Password</label>
          <input
            id="password"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
        </div>

        {error && <p className={styles.error}>{error}</p>}

        <button className={styles.submitBtn} type="submit" disabled={submitting}>
          {submitting ? 'Logging in…' : 'Log in'}
        </button>
      </form>

      <p className={styles.switchLine}>
        No account? <Link to="/register">Register</Link>
      </p>
    </AuthLayout>
  );
}
