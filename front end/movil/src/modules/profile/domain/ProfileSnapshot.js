export class ProfileSnapshot {
  constructor({ activity, profile }) {
    this.activity = activity;
    this.profile = profile;
    Object.freeze(this);
  }

  withProfile(profile) {
    return new ProfileSnapshot({ activity: this.activity, profile });
  }
}
