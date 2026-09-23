import styles from './AuthLayout.module.css';

export default function AuthLayout({ headline, subtext, children }) {
  return (
    <div className={styles.wrapper}>
      <div className={styles.visual}>
        <div className={styles.visualContent}>
          <p className={styles.brand}>TAKETWO</p>
          <h1 className={styles.headline}>{headline}</h1>
          <p className={styles.subtext}>{subtext}</p>
        </div>
      </div>
      <div className={styles.formSide}>
        <div className={styles.formCard}>{children}</div>
      </div>
    </div>
  );
}
