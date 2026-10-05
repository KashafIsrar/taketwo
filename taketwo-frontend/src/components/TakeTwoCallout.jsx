// components/TakeTwoCallout.jsx
import { Link } from 'react-router-dom';
import styles from './TakeTwoCallout.module.css';

export default function TakeTwoCallout() {
  return (
    <Link to="/take-two" className={styles.callout}>
      <h3>Not sure what to watch?</h3>
      <p>Take Two - answer four quick questions, get one film.</p>
    </Link>
  );
}