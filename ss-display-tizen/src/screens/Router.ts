import type { ContentData, Route } from '../models';
import { ContentRenderer } from '../renderers/ContentRenderer';

/** Owns screen replacement and guarantees content cleanup between application states. */
export class Router {
  public constructor(private readonly root: HTMLElement, private readonly renderer: ContentRenderer) {}
  public show(route: Route, data?: { message?: string; code?: string; url?: string; content?: ContentData }): void {
    this.renderer.stop(); this.root.replaceChildren();
    if (route === 'content' && data?.content) { this.root.className = 'content'; this.renderer.render(data.content, this.root); return; }
    this.root.className = `screen ${route}`; const card = document.createElement('section'); card.className = 'card';
    const eyebrow = document.createElement('span'); eyebrow.className = 'eyebrow'; eyebrow.textContent = route === 'activation' ? 'Connect display' : 'OnScreenSync';
    const heading = document.createElement('h1'); heading.textContent = route === 'activation' ? (data?.code ?? 'Requesting code…') : route === 'splash' ? 'Preparing your display' : 'Display unavailable';
    const message = document.createElement('p'); message.textContent = data?.url ?? data?.message ?? 'Please wait…'; card.append(eyebrow, heading, message);
    if (route === 'status') { const button = document.createElement('button'); button.id = 'retry'; button.textContent = 'Try again'; card.append(button); }
    this.root.append(card); (this.root.querySelector('button') as HTMLElement | null)?.focus();
  }
}
