import {
  ChangeMasterKeyCommand,
  ConfigureMasterKeyCommand,
  VerifyMasterKeyCommand,
} from '../domain/VaultCommands';
import {
  CreateCredentialDraft,
  UpdateCredentialDraft,
} from '../domain/CredentialDraft';
import { PasswordVault } from '../domain/PasswordVault';
import { VaultSnapshot } from '../domain/VaultSnapshot';

function requireSession(session) {
  if (!session?.accessToken) {
    throw new Error('La sesión no incluye un access token válido.');
  }
}

export class LoadVaultUseCase {
  constructor(repository, clock = () => new Date()) {
    this.repository = repository;
    this.clock = clock;
  }

  async execute(session) {
    requireSession(session);
    const access = await this.repository.status(session);
    if (!access.isUnlockedAt(this.clock())) {
      return new VaultSnapshot({ access });
    }
    const credentials = await this.repository.listCredentials(session);
    return new VaultSnapshot({
      access,
      vault: PasswordVault.create(credentials),
    });
  }
}

export class VerifyMasterKeyUseCase {
  constructor(repository) {
    this.repository = repository;
  }

  async execute(form, session) {
    requireSession(session);
    const command = VerifyMasterKeyCommand.create(form);
    const access = await this.repository.verify(command, session);
    const credentials = await this.repository.listCredentials(session);
    return new VaultSnapshot({ access, vault: PasswordVault.create(credentials) });
  }
}

export class ConfigureMasterKeyUseCase {
  constructor(repository) {
    this.repository = repository;
  }

  execute(form, session) {
    requireSession(session);
    return this.repository.configure(ConfigureMasterKeyCommand.create(form), session);
  }
}

export class ChangeMasterKeyUseCase {
  constructor(repository) {
    this.repository = repository;
  }

  execute(form, session) {
    requireSession(session);
    return this.repository.changeMasterKey(ChangeMasterKeyCommand.create(form), session);
  }
}

export class LockVaultUseCase {
  constructor(repository) {
    this.repository = repository;
  }

  execute(session) {
    requireSession(session);
    return this.repository.lock(session);
  }
}

export class RevealCredentialUseCase {
  constructor(repository) {
    this.repository = repository;
  }

  execute(credentialId, session) {
    requireSession(session);
    if (!credentialId) {
      throw new Error('La credencial no tiene un identificador válido.');
    }
    return this.repository.getCredential(credentialId, session);
  }
}

export class CreateCredentialUseCase {
  constructor(repository) {
    this.repository = repository;
  }

  execute(form, session) {
    requireSession(session);
    return this.repository.createCredential(CreateCredentialDraft.create(form), session);
  }
}

export class UpdateCredentialUseCase {
  constructor(repository) {
    this.repository = repository;
  }

  execute(credential, form, session) {
    requireSession(session);
    if (!credential?.id) {
      throw new Error('La credencial no tiene un identificador válido.');
    }
    return this.repository.updateCredential(
      credential.id,
      UpdateCredentialDraft.create(form, credential),
      session,
    );
  }
}

export class DeleteCredentialUseCase {
  constructor(repository) {
    this.repository = repository;
  }

  async execute(credentialId, session) {
    requireSession(session);
    if (!credentialId) {
      throw new Error('La credencial no tiene un identificador válido.');
    }
    await this.repository.deleteCredential(credentialId, session);
    return credentialId;
  }
}
