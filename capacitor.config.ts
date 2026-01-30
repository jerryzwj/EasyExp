import type { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'com.miniledger.app',
  appName: 'MiniLedger',
  webDir: 'out',
  server: {
    androidScheme: 'http',
    hostname: 'localhost',
    allowNavigation: ['*']
  }
};

export default config;
