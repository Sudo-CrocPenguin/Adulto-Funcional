import { PasswordVault } from './PasswordVault';

export class VaultSnapshot {
  constructor({ access, vault = PasswordVault.create() }) {
    this.access = access;
    this.vault = vault;
    Object.freeze(this);
  }
}
