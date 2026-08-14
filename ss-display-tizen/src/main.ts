import './styles.css';
import { AppController } from './app/AppController';
import { BootstrapService } from './api/BootstrapService';
import { AvPlayAdapter } from './media/AvPlayAdapter';
import { KeepAwake } from './platform/KeepAwake';
import { StorageService } from './platform/Storage';
import { ContentRenderer } from './renderers/ContentRenderer';
import { Router } from './screens/Router';

const root = document.querySelector<HTMLElement>('#app');
if (!root) throw new Error('Application root was not found');
const player = new AvPlayAdapter(); const controller = new AppController(new StorageService(), new BootstrapService(), new Router(root, new ContentRenderer(player)));
const keepAwake = new KeepAwake(); keepAwake.enable();
document.addEventListener('click', event => { if ((event.target as HTMLElement).id === 'retry') controller.retry(); });
document.addEventListener('keydown', event => {
  if (event.key === 'Enter' && document.activeElement instanceof HTMLButtonElement) document.activeElement.click();
  if (event.key === 'Escape' || event.keyCode === 10009) { try { window.tizen?.application.getCurrentApplication().exit(); } catch { window.close(); } }
});
document.addEventListener('visibilitychange', () => { if (document.hidden) player.pause(); else player.resume(); });
window.addEventListener('unload', () => keepAwake.disable(), { once: true });
void controller.start();
