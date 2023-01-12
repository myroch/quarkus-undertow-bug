/*
 * Copyright 2023 by MediData AG
 * ALL RIGHTS RESERVED
 *
 * MediData AG - CommercialSoftwareLicense
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.acme.rest.json;

import java.util.function.Supplier;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.control.ActivateRequestContext;

import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.SecurityIdentityAugmentor;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.mutiny.Uni;

@ApplicationScoped
public class SlowAugmentor implements SecurityIdentityAugmentor {

   @Override
   public Uni<SecurityIdentity> augment(SecurityIdentity identity, AuthenticationRequestContext context) {
      if (identity.isAnonymous()) {
         return Uni.createFrom().item(identity);
      }
      return context.runBlocking(build(identity));
   }

   @ActivateRequestContext
   Supplier<SecurityIdentity> build(SecurityIdentity identity) {
      QuarkusSecurityIdentity.Builder builder = QuarkusSecurityIdentity.builder(identity);
      builder.addRole("tester");
      return builder::build;
   }

   @Override
   public int priority() {
      return 500;
   }
}
