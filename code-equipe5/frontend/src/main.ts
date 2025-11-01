import { enableProdMode } from '@angular/core';
import { environment } from './environments/environment';
import { AppModule } from './app/app.module';

// JIT en dev (nécessite @angular/compiler), AOT en prod
import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';
import { platformBrowser } from '@angular/platform-browser';

if (environment.production) {
  enableProdMode();
  platformBrowser().bootstrapModule(AppModule).catch(err => console.error(err));
} else {
  platformBrowserDynamic().bootstrapModule(AppModule).catch(err => console.error(err));
}
