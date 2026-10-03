export default function Loading({ message = 'Loading…' }) {
  return (
    <div className="loading-overlay">
      <div className="spinner" aria-label="Loading" />
      <p>{message}</p>
    </div>
  );
}
