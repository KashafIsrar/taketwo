// hooks/useMessagePolling.js
import { useEffect, useRef, useState, useCallback } from 'react';
import { getMessages } from '../services/api';

const POLL_INTERVAL_MS = 4000;

export default function useMessagePolling(conversationId) {
  const [messages, setMessages] = useState([]);
  const [loading, setLoading] = useState(true);
  const cursorRef = useRef(null);
  const intervalRef = useRef(null);

  const fetchNew = useCallback(async () => {
    if (!conversationId) return;
    try {
      const newMessages = await getMessages(conversationId, cursorRef.current);
      if (newMessages.length > 0) {
        setMessages((prev) => [...prev, ...newMessages]);
        cursorRef.current = newMessages[newMessages.length - 1].createdAt;
      }
    } catch (err) {
      console.error('Failed to poll messages', err);
    }
  }, [conversationId]);

  // Initial full load on conversation change
  useEffect(() => {
    if (!conversationId) return;
    let cancelled = false;
    setLoading(true);
    setMessages([]);
    cursorRef.current = null;

    getMessages(conversationId, null)
      .then((initial) => {
        if (cancelled) return;
        setMessages(initial);
        if (initial.length > 0) cursorRef.current = initial[initial.length - 1].createdAt;
      })
      .finally(() => { if (!cancelled) setLoading(false); });

    return () => { cancelled = true; };
  }, [conversationId]);

  // Polling loop - only while the tab is actually visible, stops entirely
  // when backgrounded or the component unmounts.
  useEffect(() => {
    if (!conversationId) return;

    function startPolling() {
      if (intervalRef.current) return;
      intervalRef.current = setInterval(fetchNew, POLL_INTERVAL_MS);
    }
    function stopPolling() {
      if (intervalRef.current) {
        clearInterval(intervalRef.current);
        intervalRef.current = null;
      }
    }
    function handleVisibilityChange() {
      if (document.hidden) {
        stopPolling();
      } else {
        fetchNew();
        startPolling();
      }
    }

    if (!document.hidden) startPolling();
    document.addEventListener('visibilitychange', handleVisibilityChange);

    return () => {
      stopPolling();
      document.removeEventListener('visibilitychange', handleVisibilityChange);
    };
  }, [conversationId, fetchNew]);

  // Lets the send-message UI show the sent message immediately rather than
  // waiting up to 4s for the next poll to pick up its own send.
  const appendLocalMessage = useCallback((message) => {
    setMessages((prev) => [...prev, message]);
    cursorRef.current = message.createdAt;
  }, []);

  return { messages, loading, appendLocalMessage, refetch: fetchNew };
}