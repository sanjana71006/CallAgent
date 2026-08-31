const crypto = require('crypto');
const bcrypt = require('bcryptjs');
const mongoose = require('mongoose');
const User = require('../models/User');

const memoryUsers = new Map();

(async () => {
  const salt = await bcrypt.genSalt(10);
  const hash = await bcrypt.hash('password123', salt);
  const defaultUser = {
    userId: 'usr_sanjana_default_2026',
    name: 'Sanjana',
    email: 'sanjana@callmate.ai',
    password: hash,
    passwordHash: hash,
    phoneNumber: '9440886543',
    accountStatus: 'ACTIVE',
    createdAt: new Date('2026-08-22T10:00:00Z'),
    lastLogin: new Date(),
    comparePassword: async function (candidatePassword) {
      return bcrypt.compare(candidatePassword, this.passwordHash || this.password);
    },
    save: async function () {
      memoryUsers.set(this.email.toLowerCase(), this);
      return this;
    }
  };
  memoryUsers.set(defaultUser.email.toLowerCase(), defaultUser);
})();

const isDBReady = () => mongoose.connection && mongoose.connection.readyState === 1;

const findByEmail = async (email) => {
  const normEmail = email.trim().toLowerCase();
  if (isDBReady()) {
    try {
      const user = await User.findOne({ email: normEmail }).select('+password +passwordHash');
      if (user) return user;
    } catch (err) {
      console.warn(`[UserStore] MongoDB findByEmail fallback: ${err.message}`);
    }
  }
  return memoryUsers.get(normEmail) || null;
};

const findById = async (userId) => {
  if (isDBReady()) {
    try {
      const user = await User.findOne({ userId });
      if (user) return user;
    } catch (err) {
      console.warn(`[UserStore] MongoDB findById fallback: ${err.message}`);
    }
  }
  for (const u of memoryUsers.values()) {
    if (u.userId === userId) return u;
  }
  return null;
};

const create = async ({ name, email, password, phoneNumber }) => {
  const normEmail = email.trim().toLowerCase();
  const userId = `usr_${crypto.randomUUID()}`;
  const salt = await bcrypt.genSalt(10);
  const hash = await bcrypt.hash(password, salt);

  const memUser = {
    userId,
    name: name.trim(),
    email: normEmail,
    password: hash,
    passwordHash: hash,
    phoneNumber: phoneNumber ? phoneNumber.trim() : '',
    accountStatus: 'ACTIVE',
    createdAt: new Date(),
    lastLogin: new Date(),
    comparePassword: async function (candidatePassword) {
      return bcrypt.compare(candidatePassword, this.passwordHash || this.password);
    },
    save: async function () {
      memoryUsers.set(this.email.toLowerCase(), this);
      return this;
    }
  };
  memoryUsers.set(normEmail, memUser);

  if (isDBReady()) {
    try {
      const dbUser = await User.create({
        userId,
        name: name.trim(),
        email: normEmail,
        password,
        phoneNumber: phoneNumber ? phoneNumber.trim() : '',
        accountStatus: 'ACTIVE',
        lastLogin: new Date(),
      });
      return dbUser;
    } catch (err) {
      console.warn(`[UserStore] MongoDB create sync error: ${err.message}`);
    }
  }

  return memUser;
};

const deleteById = async (userId) => {
  for (const [email, u] of memoryUsers.entries()) {
    if (u.userId === userId) {
      memoryUsers.delete(email);
      break;
    }
  }
  if (isDBReady()) {
    try {
      await User.deleteOne({ userId });
    } catch (err) {
      console.warn(`[UserStore] MongoDB delete error: ${err.message}`);
    }
  }
};

module.exports = {
  findByEmail,
  findById,
  create,
  deleteById,
};
