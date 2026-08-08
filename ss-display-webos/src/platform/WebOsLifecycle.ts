declare global { interface Window { PalmSystem?: { platformBack(): void } } }
/** Isolates webOS-specific exit behavior from application logic. */
export class WebOsLifecycle {
  public exit(): void { if (window.PalmSystem?.platformBack) window.PalmSystem.platformBack(); else window.close(); }
}
