-- Migration 2.0, dialecte H2. L'unicite des emails est imposee par cet index.
ALTER TABLE salles ADD COLUMN description VARCHAR(255);
CREATE UNIQUE INDEX idx_utilisateur_email ON utilisateurs(email);
