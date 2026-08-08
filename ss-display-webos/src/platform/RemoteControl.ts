import { WebOsLifecycle } from './WebOsLifecycle';
/** Maps LG remote keys to accessible browser actions. */
export class RemoteControl {
  public constructor(private readonly lifecycle: WebOsLifecycle) {}
  public attach(): void { document.addEventListener('keydown', this.handleKey); }
  public detach(): void { document.removeEventListener('keydown', this.handleKey); }
  private readonly handleKey = (event: KeyboardEvent): void => {
    if (event.key === 'Enter' && document.activeElement instanceof HTMLButtonElement) document.activeElement.click();
    if (event.key === 'Escape' || event.key === 'GoBack' || event.keyCode === 461) { event.preventDefault(); this.lifecycle.exit(); }
  };
}
