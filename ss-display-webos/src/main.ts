import './styles.css';
import { AppController } from './app/AppController';
import { BootstrapService } from './api/BootstrapService';
import { HtmlVideoPlayer } from './media/HtmlVideoPlayer';
import { StorageService } from './platform/Storage';
import { RemoteControl } from './platform/RemoteControl';
import { WebOsLifecycle } from './platform/WebOsLifecycle';
import { ContentRenderer } from './renderers/ContentRenderer';
import { Router } from './screens/Router';

const root = document.querySelector<HTMLElement>('#app');
if (!root) throw new Error('Application root was not found');
const player = new HtmlVideoPlayer();
const controller = new AppController(new StorageService(), new BootstrapService(), new Router(root, new ContentRenderer(player)));
const remote = new RemoteControl(new WebOsLifecycle()); remote.attach();
document.addEventListener('click', event => { if ((event.target as HTMLElement).id === 'retry') controller.retry(); });
document.addEventListener('visibilitychange', () => { if (document.hidden) player.pause(); else player.resume(); });
window.addEventListener('unload', () => remote.detach(), { once: true });
void controller.start();
