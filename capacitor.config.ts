import type { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'com.miniledger.app',
  appName: 'MiniLedger',
  webDir: 'out',
  server: {
    androidScheme: 'https',
    hostname: 'zd.wenjun.eu.org'
  }
};

export default config;
