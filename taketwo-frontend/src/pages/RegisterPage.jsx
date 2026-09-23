import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import AuthLayout from '../components/AuthLayout.jsx';
import styles from '../components/AuthLayout.module.css';

export default function RegisterPage() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ username: '', email: '', password: '' });
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  function update(field) {
    return (e) => setForm((prev) => ({ ...prev, [field]: e.target.value }));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    setSubmitting(true);
    try {
      await register(form);
      navigate('/');
    } catch (err) {
      setError(err.response?.data?.message || 'Registration failed');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <AuthLayout
      headline="Every film leaves a mark."
      subtext="Start your diary, build your taste profile, and find the cinephiles who watch like you do."
    >
      <h1>Join TAKETWO</h1>
      <p className={styles.formSubtitle}>Create an account to start logging.</p>

      <form onSubmit={handleSubmit}>
        <div className={styles.field}>
          <label htmlFor="username">Username</label>
          <input id="username" value={form.username} onChange={update('username')} />
        </div>
        <div className={styles.field}>
          <label htmlFor="email">Email</label>
          <input id="email" type="email" value={form.email} onChange={update('email')} />
        </div>
        <div className={styles.field}>
          <label htmlFor="password">Password</label>
          <input id="password" type="password" value={form.password} onChange={update('password')} />
        </div>

        {error && <p className={styles.error}>{error}</p>}

        <button className={styles.submitBtn} type="submit" disabled={submitting}>
          {submitting ? 'Creating account…' : 'Create account'}
        </button>
      </form>

      <p className={styles.switchLine}>
        Already have an account? <Link to="/login">Log in</Link>
      </p>
    </AuthLayout>
  );
}
