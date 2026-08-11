import { useCallback, useEffect, useRef, useState } from 'react';
import { AppState } from 'react-native';

import { useAppTheme } from '../../../theme/AppThemeContext';
import { UPDATE_PHASES, UPDATE_RESULTS } from '../application/EnsureLatestUpdateUseCase';
import { MandatoryUpdateScreen } from './MandatoryUpdateScreen';

const ACTIVE_UPDATE_CHECK_INTERVAL_MS = 5 * 60 * 1000;

export function MandatoryUpdateGate({ children, ensureLatestUpdate }) {
  const { palette } = useAppTheme();
  const appState = useRef(AppState.currentState);
  const mounted = useRef(true);
  const running = useRef(false);
  const [phase, setPhase] = useState(UPDATE_PHASES.checking);

  const verify = useCallback(async () => {
    if (running.current) return;
    running.current = true;
    if (mounted.current) setPhase(UPDATE_PHASES.checking);

    try {
      const result = await ensureLatestUpdate.execute({
        onPhase: (nextPhase) => {
          if (mounted.current) setPhase(nextPhase);
        },
      });
      if (mounted.current && result.status !== UPDATE_RESULTS.restarting) {
        setPhase('ready');
      }
    } catch {
      if (mounted.current) setPhase('blocked');
    } finally {
      running.current = false;
    }
  }, [ensureLatestUpdate]);

  useEffect(() => {
    mounted.current = true;
    verify();
    return () => {
      mounted.current = false;
    };
  }, [verify]);

  useEffect(() => {
    const subscription = AppState.addEventListener('change', (nextState) => {
      const becameActive = appState.current !== 'active' && nextState === 'active';
      appState.current = nextState;
      if (becameActive) verify();
    });
    return () => subscription.remove();
  }, [verify]);

  useEffect(() => {
    const interval = setInterval(() => {
      if (appState.current === 'active') verify();
    }, ACTIVE_UPDATE_CHECK_INTERVAL_MS);

    return () => clearInterval(interval);
  }, [verify]);

  if (phase === 'ready') return children;

  return <MandatoryUpdateScreen onRetry={verify} palette={palette} phase={phase} />;
}
